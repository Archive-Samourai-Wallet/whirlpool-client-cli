package com.samourai.whirlpool.cli.services;

import com.samourai.wallet.httpClient.HttpProxy;
import com.samourai.wallet.util.SystemUtil;
import com.samourai.whirlpool.cli.Application;
import com.samourai.whirlpool.cli.ApplicationArgs;
import com.samourai.whirlpool.cli.beans.CliResult;
import com.samourai.whirlpool.cli.beans.CliStatus;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.AuthenticationException;
import com.samourai.whirlpool.cli.exception.CliRestartException;
import com.samourai.whirlpool.cli.exception.NoUserInputException;
import com.samourai.whirlpool.cli.run.CliStatusOrchestrator;
import com.samourai.whirlpool.cli.run.RunCliCommand;
import com.samourai.whirlpool.cli.run.RunCliInit;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int CLI_STATUS_DELAY = 5000;

  private ApplicationArgs appArgs;
  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private CliWalletService cliWalletService;
  private CliUpgradeService cliUpgradeService;
  private CliTorClientService cliTorClientService;
  private JavaHttpClientService httpClientService;
  private CliStatusOrchestrator cliStatusOrchestrator;

  public CliService(
      ApplicationArgs appArgs,
      CliConfig cliConfig,
      CliConfigService cliConfigService,
      CliWalletService cliWalletService,
      CliUpgradeService cliUpgradeService,
      CliTorClientService cliTorClientService,
      JavaHttpClientService httpClientService) {
    this.appArgs = appArgs;
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.cliWalletService = cliWalletService;
    this.cliUpgradeService = cliUpgradeService;
    this.cliTorClientService = cliTorClientService;
    this.httpClientService = httpClientService;
    this.cliStatusOrchestrator = null;
    init();
  }

  private void init() {
    // properties were set on CliConfig => override CliConfig with cli args
    appArgs.override(cliConfig);

    // setup proxy
    Optional<HttpProxy> cliProxyOptional = cliConfig.getCliProxy();
    if (cliProxyOptional.isPresent()) {
      HttpProxy cliProxy = cliProxyOptional.get();
      CliUtils.useProxy(cliProxy);
    }
  }

  public void setup() throws Exception {
    // setup Tor
    cliTorClientService.setup();
  }

  private File computeDirLockFile() throws NotifiableException {
    String path = "whirlpool-cli.lock";
    return ClientUtils.createFile(path);
  }

  public FileLock lockDirectory() throws Exception {
    File dirLockFile = computeDirLockFile();
    dirLockFile.deleteOnExit();
    String lockErrorMsg =
        "Another Whirlpool instance seems already running in same directory. If not, please delete "
            + dirLockFile.getAbsolutePath();
    FileLock dirFileLock = SystemUtil.lockFile(dirLockFile, lockErrorMsg);
    return dirFileLock;
  }

  public void unlockDirectory(FileLock dirFileLock) throws Exception {
    SystemUtil.unlockFile(dirFileLock);
    File dirLockFile = computeDirLockFile();
    dirLockFile.delete();
  }

  public void run(boolean listen) {
    Thread t =
        new Thread(
            () -> {
              try {
                CliResult cliResult = doRun(listen);
                switch (cliResult) {
                  case RESTART:
                    Application.restart();
                    return;
                  case EXIT_SUCCESS:
                    Application.exit(0);
                    return;
                  case KEEP_RUNNING:
                    synchronized (this) {
                      wait();
                    }
                    return;
                }
              } catch (NotifiableException e) {
                CliUtils.notifyError(e.getMessage());
              } catch (IllegalArgumentException e) {
                log.error("Invalid arguments: " + e.getMessage());
              } catch (Exception e) {
                log.error("", e);
              }
              Application.exit(1); // error
            });
    t.setName("CliService.run");
    t.start();
  }

  protected CliResult doRun(boolean listen) throws Exception {
    String[] args = appArgs.getApplicationArguments().getSourceArgs();

    log.info("------------ whirlpool-client-cli starting ------------");
    log.info(
        "Running whirlpool-client-cli " + cliConfig.getBuildVersion() + " on java {}... {}",
        System.getProperty("java.version"),
        Arrays.toString(args));

    // log config
    if (log.isDebugEnabled()) {
      for (Map.Entry<String, String> entry : cliConfig.getConfigInfo().entrySet()) {
        log.debug("[cliConfig/" + entry.getKey() + "] " + entry.getValue());
      }
    }
    if (listen) {
      String info = "API is listening on https://127.0.0.1:" + cliConfig.getApi().getPort();
      if (cliConfig.getApi().isHttpEnable()) {
        info += " and http://127.0.0.1:" + cliConfig.getApi().getHttpPort();
      }
      log.info(info);
    }

    // connect Tor
    cliTorClientService.connect();

    // initialize bitcoinj context
    NetworkParameters params = cliConfig.getSamouraiNetwork().getParams();
    new Context(params);

    // check init
    if (appArgs.isInit()) {
      new RunCliInit(cliConfigService, cliConfig).runPairExisting();
      return CliResult.RESTART;
    }

    // check CLI initialized
    if (cliConfigService.isCliStatusNotInitialized() && !listen) {
      log.info("⣿ CLI NOT INITIALIZED");
      log.info("⣿ Use --init to pair with an existing wallet");
      return CliResult.EXIT_SUCCESS;
    }

    // check cli initialized
    if (cliConfigService.isCliStatusNotInitialized()) {
      // not initialized
      if (log.isDebugEnabled()) {
        log.debug("CliStatus=" + cliConfigService.getCliStatus());
      }

      // keep cli running for remote initialization
      log.warn(CliUtils.LOG_SEPARATOR);
      log.warn("⣿ INITIALIZATION REQUIRED");
      log.warn("⣿ Please start GUI to initialize CLI.");
      log.warn("⣿ Or initialize with --init");
      log.warn(CliUtils.LOG_SEPARATOR);
      keepRunning();
      return CliResult.KEEP_RUNNING;
    }

    // check upgrade (before authentication)
    boolean shouldRestart = cliUpgradeService.upgradeUnauthenticated();
    if (shouldRestart) {
      log.warn(CliUtils.LOG_SEPARATOR);
      log.warn("⣿ UPGRADE SUCCESS");
      log.warn("⣿ Restarting CLI...");
      log.warn(CliUtils.LOG_SEPARATOR);
      return CliResult.RESTART;
    }
    cliConfigService.setCliStatus(CliStatus.READY);

    String commandToRun = RunCliCommand.getCommandToRun(appArgs);
    boolean hasCommandToRun = (commandToRun != null);
    if (!appArgs.isAuthenticate() && listen && commandToRun == null) {
      // no passphrase but listening => keep listening
      return keepRunningNoAuth();
    }

    // authenticate
    String seedPassphrase = null;
    CliWallet cliWallet = null;
    while (cliWallet == null) {
      // authenticate to open wallet
      String reason = null;
      if (commandToRun != null) {
        reason = "to run --" + commandToRun;
      }

      try {
        seedPassphrase = authenticate(reason);
      } catch (NoUserInputException e) {
        // no user input available
        return keepRunningNoAuth();
      }
      try {
        // we may have authenticated from API in the meantime...
        cliWallet =
            cliWalletService.hasSessionWallet()
                ? cliWalletService.getSessionWallet()
                : cliWalletService.openWallet(seedPassphrase);

        log.info(CliUtils.LOG_SEPARATOR);
        log.info("⣿ AUTHENTICATION SUCCESS");
        log.info(CliUtils.LOG_SEPARATOR);
      } catch (AuthenticationException e) {
        log.error(e.getMessage());
      } catch (CliRestartException e) {
        log.error(e.getMessage());
        return CliResult.RESTART;
      }
    }
    if (hasCommandToRun) {
      // execute specific command
      new RunCliCommand(appArgs, cliWalletService, cliConfigService, cliConfig)
          .run(params, seedPassphrase);
      return CliResult.EXIT_SUCCESS;
    } else {
      // start wallet
      log.info("⣿ Whirlpool is starting...");
      cliWallet.startAsync().blockingAwait();
      keepRunning();
      return CliResult.KEEP_RUNNING;
    }
  }

  private CliResult keepRunningNoAuth() {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ AUTHENTICATION REQUIRED");
    log.info("⣿ Whirlpool wallet is CLOSED.");
    log.info("⣿ Please start GUI to authenticate and start mixing.");
    log.info("⣿ Or authenticate with --authenticate");
    log.info(CliUtils.LOG_SEPARATOR);
    keepRunning();
    return CliResult.KEEP_RUNNING;
  }

  private void keepRunning() {
    // disable statusOrchestrator when redirecting output
    if (CliUtils.hasConsole()) {
      // log status
      this.cliStatusOrchestrator =
          new CliStatusOrchestrator(CLI_STATUS_DELAY, cliWalletService, cliConfig);
      this.cliStatusOrchestrator.start(true);
    }
  }

  private String authenticate(String reason) {
    if (reason == null) {
      reason = "to authenticate and start mixing.";
    }
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ AUTHENTICATION REQUIRED");
    log.info("⣿ Whirlpool wallet is CLOSED.");
    log.info("⣿ • Type your passphrase " + reason);
    return CliUtils.readUserInputRequired("Passphrase?", true);
  }

  public void shutdown() {
    log.info("------------ whirlpool-client-cli ending ------------");
    if (log.isDebugEnabled()) {
      log.debug("shutdown");
    }

    // disconnect Tor
    if (cliTorClientService != null) {
      if (log.isDebugEnabled()) {
        log.debug("shutting down: Tor");
      }
      cliTorClientService.shutdown();
    }

    // close cliWallet
    if (log.isDebugEnabled()) {
      log.debug("shutting down: cliWallet");
    }
    cliWalletService.closeWallet();

    // stop httpClient
    if (log.isDebugEnabled()) {
      log.debug("shutting down: httpClient");
    }
    httpClientService.stop();

    // stop cliStatusOrchestrator
    if (cliStatusOrchestrator != null) {
      if (log.isDebugEnabled()) {
        log.debug("shutting down: cliStatusOrchestrator");
      }
      cliStatusOrchestrator.stop();
    }
  }
}

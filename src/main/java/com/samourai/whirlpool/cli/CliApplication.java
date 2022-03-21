package com.samourai.whirlpool.cli;

import com.samourai.whirlpool.cli.beans.CliResult;
import com.samourai.whirlpool.cli.services.CliArgs;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.protocol.WhirlpoolProtocol;
import java.lang.invoke.MethodHandles;
import java.nio.channels.FileLock;
import java.util.Arrays;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/** Main application. */
@SpringBootApplication
@ServletComponentScan(value = "com.samourai.whirlpool.cli.config.filters")
public class CliApplication implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static boolean listen;
  private static boolean debug;
  private static boolean debugClient;
  private static ConfigurableApplicationContext applicationContext;
  private static ApplicationArguments applicationArguments;
  private static boolean restart;
  private static Integer exitCode;
  private static FileLock fileLock;

  @Autowired Environment env;
  @Autowired CliService cliService;

  public static void main(String... args) {
    args = adaptArgs(args);

    // override configuration with local file
    System.setProperty(
        "spring.config.location",
        "classpath:application.properties,optional:./" + CliConfigService.CLI_CONFIG_FILENAME);

    // start REST api if --listen
    listen = CliArgs.getMainListen(args);

    // enable debug logs with --debug
    debug = CliArgs.isMainDebug(args);
    debugClient = CliArgs.isMainDebugClient(args);
    CliUtils.setLogLevel(debug, debugClient);

    // run
    WebApplicationType wat = listen ? WebApplicationType.SERVLET : WebApplicationType.NONE;
    applicationContext =
        new SpringApplicationBuilder(CliApplication.class).logStartupInfo(false).web(wat).run(args);

    if (restart) {
      // restart
      restart();
    } else {
      if (exitCode != null) {
        // exit
        exit(exitCode);
      } else {
        // success
        if (log.isDebugEnabled()) {
          log.debug("CLI startup complete.");
        }
      }
    }
  }

  @PreDestroy
  public void preDestroy() {
    // unlock directory
    if (fileLock != null) {
      try {
        cliService.unlockDirectory(fileLock);
      } catch (Exception e) {
        log.error("", e);
      }
    }

    // shutdown
    cliService.shutdown();
  }

  @Override
  public void run(ApplicationArguments applicationArguments) {
    restart = false;

    CliApplication.applicationArguments = applicationArguments;
    CliUtils.setLogLevel(debug, debugClient); // run twice to fix incorrect log level

    if (log.isDebugEnabled()) {
      log.debug("Run... " + Arrays.toString(applicationArguments.getSourceArgs()));
    }
    if (log.isDebugEnabled()) {
      log.debug("[cli/debug] debug=" + debug + ", debugClient=" + debugClient);
      log.debug("[cli/protocolVersion] " + WhirlpoolProtocol.PROTOCOL_VERSION);
      log.debug("[cli/listen] " + listen);
    }

    try {
      // setup Tor etc...
      cliService.setup();

      if (env.acceptsProfiles(CliUtils.SPRING_PROFILE_TESTING)) {
        log.info("Running unit test...");
        return;
      }

      fileLock = cliService.lockDirectory();

      CliResult cliResult = cliService.run(listen);
      switch (cliResult) {
        case RESTART:
          restart = true;
          break;
        case EXIT_SUCCESS:
          exitCode = 0;
          break;
        case KEEP_RUNNING:
          break;
      }
    } catch (NotifiableException e) {
      exitCode = 1;
      CliUtils.notifyError(e.getMessage());
    } catch (IllegalArgumentException e) {
      exitCode = 1;
      log.error("Invalid arguments: " + e.getMessage());
    } catch (Exception e) {
      exitCode = 1;
      log.error("", e);
    }
  }

  public static void restart() {
    long restartDelay = 1000;
    if (log.isDebugEnabled()) {
      log.debug("Restarting CLI in " + restartDelay + "ms");
    }

    // wait for restartDelay
    try {
      Thread.sleep(restartDelay);
    } catch (InterruptedException e) {
    }

    // restart application
    log.info("Restarting CLI...");
    Thread thread =
        new Thread(
            () -> {
              if (applicationContext != null) {
                applicationContext.close();
              }

              String[] restartArgs = computeRestartArgs();
              main(restartArgs);
            });
    thread.setDaemon(false);
    thread.start();
  }

  public static void exit(int exitCode) {
    if (log.isDebugEnabled()) {
      log.debug("Exit: " + exitCode);
    }
    if (applicationContext != null) {
      SpringApplication.exit(applicationContext, () -> exitCode);
    }
    System.exit(exitCode);
  }

  private static String[] computeRestartArgs() {
    String[] ignoreArgs =
        new String[] {
          "--" + CliArgs.ARG_INIT,
          "--" + CliArgs.ARG_CREATE,
          "--" + CliArgs.ARG_RESTORE_BACKUP,
          "--" + CliArgs.ARG_RESTORE_EXTERNAL,
          "--" + CliArgs.ARG_SET_EXTERNAL_XPUB
        };
    return Arrays.stream(applicationArguments.getSourceArgs())
        .filter(a -> !ArrayUtils.contains(ignoreArgs, a.toLowerCase()))
        .toArray(i -> new String[i]);
  }

  private static String[] adaptArgs(String... args) {
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        // rename Spring arg: logging.file => logging.file.name
        arg = arg.replace("--logging.file=", "--logging.file.name=");
        args[i] = arg;
      }
    }
    return args;
  }
}

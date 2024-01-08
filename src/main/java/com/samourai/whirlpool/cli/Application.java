package com.samourai.whirlpool.cli;

import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.protocol.SorobanProtocolWhirlpool;
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
public class Application implements ApplicationRunner {
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
    listen = ApplicationArgs.getMainListen(args);

    // enable debug logs with --debug
    debug = ApplicationArgs.isMainDebug(args);
    debugClient = ApplicationArgs.isMainDebugClient(args);
    CliUtils.setLogLevel(debug, debugClient);

    // run
    WebApplicationType wat = listen ? WebApplicationType.SERVLET : WebApplicationType.NONE;
    applicationContext =
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).web(wat).run(args);

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
      }
    }

    // shutdown
    cliService.shutdown();
  }

  @Override
  public void run(ApplicationArguments applicationArguments) {
    restart = false;

    Application.applicationArguments = applicationArguments;
    CliUtils.setLogLevel(debug, debugClient); // run twice to fix incorrect log level

    if (log.isDebugEnabled()) {
      log.debug("Run... " + Arrays.toString(applicationArguments.getSourceArgs()));
    }
    if (log.isDebugEnabled()) {
      log.debug("[cli/debug] debug=" + debug + ", debugClient=" + debugClient);
      log.debug("[cli/protocolVersion] " + SorobanProtocolWhirlpool.PROTOCOL_VERSION);
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
    } catch (Exception e) {
      exitCode = 1;
      log.error("", e);
    }

    // CliService runs in a new thread to immediately set applicationContext
    cliService.run(listen);
  }

  public static void restart() {
    long restartDelay = 1000;
    if (log.isDebugEnabled()) {
      log.debug("Restarting CLI in " + restartDelay + "ms");
    }

    // restartDelay
    try {
      Thread.sleep(restartDelay);
    } catch (InterruptedException e) {
    }

    // restart application
    log.info("Shutting down for restart...");
    Thread thread =
        new Thread(
            () -> {
              CliUtils.closeApplicationContext(applicationContext);
              if (log.isDebugEnabled()) {
                log.debug("Shutdown completed, restarting in " + restartDelay + " ms");
              }

              // closingDelay
              long closingDelay = 1000;
              try {
                Thread.sleep(closingDelay);
              } catch (InterruptedException e) {
              }

              if (log.isDebugEnabled()) {
                log.debug("Restarting CLI...");
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
          "--" + ApplicationArgs.ARG_INIT, "--" + ApplicationArgs.ARG_SET_EXTERNAL_XPUB
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

package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunSetExternalXpubEnabled {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfigService cliConfigService;

  public RunSetExternalXpubEnabled(CliConfigService cliConfigService) {
    this.cliConfigService = cliConfigService;
  }

  public void run(CliConfig cliConfig, boolean enabled) throws Exception {
    if (!cliConfig.isExternalDestinationConfigured()) {
      log.error("No external XPub configured yet. Use --set-external-xpub to configure.");
      throw new NotifiableException("Aborted");
    }

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION");
    log.info("⣿ ");
    log.info(
        "⣿ This will " + (enabled ? "ENABLE" : "DISABLE") + " external mixing to external XPub.");

    // validate
    boolean validate = CliUtils.readUserInputRequiredBoolean("Continue?");
    if (!validate) {
      throw new NotifiableException("Aborted");
    }

    // set configuration
    cliConfigService.setExternalDestinationDisabled(!enabled);

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION SUCCESS");
    log.info(CliUtils.LOG_SEPARATOR);
  }
}

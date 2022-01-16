package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunCliInit {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfigService cliConfigService;
  private CliConfig cliConfig;

  public RunCliInit(CliConfigService cliConfigService, CliConfig cliConfig) {
    this.cliConfigService = cliConfigService;
    this.cliConfig = cliConfig;
  }

  public void runPairExisting() throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ CLI INITIALIZATION");
    log.info("⣿ This will intialize CLI by connecting an existing Samourai Wallet.");
    log.info("⣿ ");

    // pairing payload
    WhirlpoolPairingPayload pairing = readParingPayload();
    doInit(pairing);
  }

  public void doInit(WhirlpoolPairingPayload pairing) throws Exception {
    // Tor
    boolean tor = readTor(pairing.getDojo() != null);

    // init
    String apiKey = cliConfigService.initialize(pairing, tor, null);

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ INITIALIZATION SUCCESS");
    log.info("⣿ Take note of your API Key, to connect remotely from GUI or API.");
    log.info("⣿ Your API key is: " + apiKey);
    log.info("⣿ ");
    log.info("⣿ Restarting CLI...");
    log.info(CliUtils.LOG_SEPARATOR);
  }

  private WhirlpoolPairingPayload readParingPayload() throws Exception {
    log.info(
        "⣿ Get your pairing payload in Samourai Wallet, go to 'Settings/Transactions/Experimental'");
    log.info("⣿ • Paste your pairing payload:");
    String pairingPayload = CliUtils.readUserInputRequired("Pairing payload?", false);
    log.info("⣿ ");
    return cliConfigService.parsePairingPayload(pairingPayload);
  }

  private boolean readTor(boolean dojo) {
    boolean tor;
    if (dojo) {
      // dojo => Tor enabled
      log.info("⣿ Enabling Tor (required for Dojo).");
      tor = true;
    } else {
      // samourai backend => Tor optional
      log.info("⣿ • Enable Tor? (you can change this later)");
      tor = CliUtils.readUserInputRequiredBoolean("Enable Tor?");
      log.info("⣿ ");
    }
    return tor;
  }
}

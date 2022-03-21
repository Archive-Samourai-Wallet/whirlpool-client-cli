package com.samourai.whirlpool.cli.run;

import com.samourai.whirlpool.cli.beans.WhirlpoolPairingPayload;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunDumpPayload {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfigService cliConfigService;

  public RunDumpPayload(CliConfigService cliConfigService) {
    this.cliConfigService = cliConfigService;
  }

  public void run() throws Exception {
    WhirlpoolPairingPayload pairingPayload = cliConfigService.computePairingPayload();
    String jsonPairingPayload = ClientUtils.toJsonString(pairingPayload);
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ DUMP-PAYLOAD");
    log.info("⣿ Pairing-payload of your current wallet:");
    log.info("⣿ " + jsonPairingPayload);
    log.info(CliUtils.LOG_SEPARATOR);
  }
}

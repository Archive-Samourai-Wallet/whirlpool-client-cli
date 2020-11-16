package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.XPubUtil;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunSetExternalXpub {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final FormatsUtilGeneric formatUtil = FormatsUtilGeneric.getInstance();
  private static final XPubUtil xPubUtil = XPubUtil.getInstance();

  private CliConfigService cliConfigService;

  public RunSetExternalXpub(CliConfigService cliConfigService) {
    this.cliConfigService = cliConfigService;
  }

  public void run(NetworkParameters params, String passphrase) throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION");
    log.info("⣿ This will configure an external XPub as destination for your mixed funds.");
    log.info("⣿ This XPub will remain encrypted and private.");
    log.info(
        "⣿ It will not be shared with the Whirlpool coordinator, the Samourai backend server or your own Dojo.");
    log.info("⣿ ");

    // xpub
    log.info("⣿ • Paste external BIP84 XPub here (or <enter> to unset current destination):");
    String xpub = readXpub();
    if (xpub != null) {
      // chain
      log.info("⣿ • Chain for XPub derivation path m/84'/<chain>' (use 0 for standard):");
      int chain = CliUtils.readUserInputRequiredInt("Chain?(0)", 0, 0);
      log.info("⣿ ");

      // startIndex
      log.info(
          "⣿ • Starting index for XPub derivation path m/84'/"
              + chain
              + "'/<starting index>' (use 0 for standard):");
      int startIndex = CliUtils.readUserInputRequiredInt("Starting index?(0)", 0, 0);
      log.info("⣿ ");

      // mixs
      log.info("⣿ • Number of mixs to achieve before sending funds to XPub (>0):");
      int mixs = CliUtils.readUserInputRequiredInt("Mixs?", 1);
      log.info("⣿ ");

      // print addresses
      log.info(CliUtils.LOG_SEPARATOR);
      log.info("⣿ WARNING!");
      log.info(CliUtils.LOG_SEPARATOR);
      log.info(
          "⣿ Your funds will be automatically sent to external XPub after being mixed *at least* "
              + mixs
              + " times. This number may randomly slightly increase to improve your privacy.");
      log.info("⣿ XPub: " + xpub);
      log.info("⣿ Derivation path: m/84'/" + chain + "'/" + startIndex + "+'");
      log.info("⣿ Sample destination addresses:");
      for (int i = startIndex; i < startIndex + 3; i++) {
        String address = xPubUtil.getAddressBech32(xpub, i, chain, params);
        log.info("⣿ m/84'/" + chain + "'/" + i + "'" + ": " + address);
      }

      // validate
      boolean validate = CliUtils.readUserInputRequiredBoolean("Continue? (y/n)");
      if (!validate) {
        throw new NotifiableException("Aborted");
      }

      // set configuration
      cliConfigService.setExternalDestination(xpub, chain, startIndex, mixs, passphrase);
    } else {
      log.info("⣿ This will unset external XPub. Your funds will stay on your POSTMIX wallet.");

      // validate
      boolean validate = CliUtils.readUserInputRequiredBoolean("Continue? (y/n)");
      if (!validate) {
        throw new NotifiableException("Aborted");
      }

      // clear configuration
      cliConfigService.clearExternalDestination();
    }

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION SUCCESS");
    log.info("⣿ ");
    log.info("⣿ Restarting CLI...");
    log.info(CliUtils.LOG_SEPARATOR);
  }

  private String readXpub() {
    while (true) {
      String input = CliUtils.readUserInput("XPub or <enter> to unset?", false);
      if (StringUtils.isEmpty(input)) {
        // clear current xpub
        return null;
      } else {
        try {
          if (!formatUtil.isValidXpub(input)) {
            throw new NotifiableException("Invalid BIP84 XPub");
          }
          return input;
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        log.info("⣿ ");
      }
    }
  }
}

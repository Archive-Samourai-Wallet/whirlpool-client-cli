package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.XPubUtil;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bouncycastle.util.Arrays;
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

  public void run(NetworkParameters params, CliWallet cliWallet, String passphrase)
      throws Exception {
    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION");
    log.info("⣿ This will configure an external XPub as destination for your mixed funds.");
    log.info("⣿ This XPub will remain encrypted and private.");
    log.info(
        "⣿ It will not be shared with the Whirlpool coordinator, the Samourai backend server or your own Dojo.");
    log.info("⣿ ");

    // xpub
    log.info("⣿ • Paste external BIP84 XPub here (or <enter> to unset current destination):");
    String xpub = readXpub(params);
    if (xpub != null) {
      // chain
      log.info("⣿ • Chain for derivation path m/84'/..'/..'/<chain>/... (use 0 for standard):");
      int chain = CliUtils.readUserInputRequiredInt("Chain?(0)", 0, 0);
      log.info("⣿ ");

      // startIndex
      log.info(
          "⣿ • Starting index for derivation path m/84'/..'/"
              + chain
              + "/<starting index> (use 0 for standard):");
      int startIndex = CliUtils.readUserInputRequiredInt("Starting index?(0)", 0, 0);
      log.info("⣿ ");

      // mixs
      log.info("⣿ • Number of mixs to achieve before sending funds:");
      int mixs = CliUtils.readUserInputRequiredInt("Mixs?(>0)", 1);
      log.info("⣿ ");

      // print addresses
      log.info(CliUtils.LOG_SEPARATOR);
      log.info("⣿ WARNING!");
      log.info(CliUtils.LOG_SEPARATOR);
      log.info(
          "⣿ Your funds will be automatically sent to external XPub after *at least* "
              + mixs
              + " (re)mixs. This threshold may randomly slightly increase to improve your privacy.");
      log.info("⣿ XPub: " + xpub);
      log.info("⣿ Derivation path: m/84'/...'/...'/" + chain + "/" + startIndex + "+");
      log.info("⣿ Sample destination addresses:");
      for (int i = startIndex; i < startIndex + 3; i++) {
        String address = xPubUtil.getAddressBech32(xpub, i, chain, params);
        log.info("⣿ m/84'/...'/...'/" + chain + "/" + i + ": " + address);
      }

      // validate
      boolean validate = CliUtils.readUserInputRequiredBoolean("Continue?");
      if (!validate) {
        throw new NotifiableException("Aborted");
      }

      // set configuration
      cliConfigService.setExternalDestination(xpub, chain, startIndex, mixs, passphrase);

      // set next postmix index
      cliWallet.getWalletStateSupplier().getIndexHandlerExternal().set(startIndex, true);
    } else {
      log.info("⣿ This will unset external XPub. Your funds will stay on your POSTMIX wallet.");

      // validate
      boolean validate = CliUtils.readUserInputRequiredBoolean("Continue?");
      if (!validate) {
        throw new NotifiableException("Aborted");
      }

      // clear configuration
      cliConfigService.clearExternalDestination();

      // clear postmix index
      cliWallet.getWalletStateSupplier().getIndexHandlerExternal().set(0, true);
    }

    log.info(CliUtils.LOG_SEPARATOR);
    log.info("⣿ EXTERNAL XPUB CONFIGURATION SUCCESS");
    log.info(CliUtils.LOG_SEPARATOR);
  }

  private String readXpub(NetworkParameters params) {
    while (true) {
      String input = CliUtils.readUserInput("External BIP84 XPub/ZPub or <enter> to unset?", false);
      if (StringUtils.isEmpty(input)) {
        // clear current xpub
        return null;
      } else {
        try {
          // if (!formatUtil.isValidXpub(input)) {
          if (!isValidXpubOrZpub(input, params)) {
            throw new NotifiableException("Invalid BIP84 XPub/ZPub");
          }
          return input;
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        log.info("⣿ ");
      }
    }
  }

  // TODO use extlibj
  private static final int MAGIC_ZPUB = 0x04B24746;
  private static final int MAGIC_VPUB = 0x045F1CF6;
  public static final int MAGIC_TPUB = 0x043587CF;
  public static final int MAGIC_XPUB = 0x0488B21E;

  public boolean isValidXpubOrZpub(String xpub, NetworkParameters params) {
    int[] magic =
        formatUtil.isTestNet(params)
            ? new int[] {MAGIC_VPUB, MAGIC_TPUB}
            : new int[] {MAGIC_ZPUB, MAGIC_XPUB};
    return isValidXpub(xpub, magic);
  }

  private boolean isValidXpub(String xpub, int... versions) {

    try {
      byte[] xpubBytes = Base58.decodeChecked(xpub);

      if (xpubBytes.length != 78) {
        return false;
      }

      ByteBuffer byteBuffer = ByteBuffer.wrap(xpubBytes);
      int version = byteBuffer.getInt();
      if (!Arrays.contains(versions, version)) {
        throw new AddressFormatException("invalid version: " + xpub);
      } else {

        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // depth:
        byteBuffer.get();
        // parent fingerprint:
        byteBuffer.getInt();
        // child no.
        byteBuffer.getInt();
        byteBuffer.get(chain);
        byteBuffer.get(pub);

        ByteBuffer pubBytes = ByteBuffer.wrap(pub);
        int firstByte = pubBytes.get();
        if (firstByte == 0x02 || firstByte == 0x03) {
          return true;
        } else {
          return false;
        }
      }
    } catch (Exception e) {
      return false;
    }
  }
}

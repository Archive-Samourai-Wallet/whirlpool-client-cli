package com.samourai.whirlpool.cli.beans;

import com.samourai.wallet.api.pairing.*;
import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.lang.invoke.MethodHandles;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhirlpoolPairingPayload extends PairingPayload {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public WhirlpoolPairingPayload() {
    super();
  }

  public WhirlpoolPairingPayload(
      PairingNetwork network, String mnemonicEncrypted, Boolean passphrase, PairingDojo dojo) {
    super(
        PairingType.WHIRLPOOL_GUI,
        PairingVersion.V3_0_0,
        network,
        mnemonicEncrypted,
        passphrase,
        dojo);
  }

  public static WhirlpoolPairingPayload newInstance(
      String mnemonic,
      String passphrase,
      boolean appendPassphrase,
      PairingDojo dojo,
      NetworkParameters params)
      throws Exception {
    String mnemonicEncrypted = encryptSeedWords(mnemonic, passphrase);

    PairingNetwork network =
        FormatsUtilGeneric.getInstance().isTestNet(params)
            ? PairingNetwork.TESTNET
            : PairingNetwork.MAINNET;

    return new WhirlpoolPairingPayload(network, mnemonicEncrypted, appendPassphrase, dojo);
  }

  protected static String encryptSeedWords(String seedWords, String seedPassphrase)
      throws Exception {
    String encrypted = AESUtil.encrypt(seedWords, new CharSequenceX(seedPassphrase));
    return encrypted;
  }

  public static WhirlpoolPairingPayload parse(String pairingPayloadStr) throws NotifiableException {
    WhirlpoolPairingPayload pairingPayload;
    try {
      pairingPayload = ClientUtils.fromJson(pairingPayloadStr, WhirlpoolPairingPayload.class);
    } catch (NotifiableException e) {
      throw e;
    } catch (Exception e) {
      log.error("", e);
      throw new NotifiableException("Invalid pairing payload");
    }

    // passphrase=true for V1
    if (pairingPayload.getPairing().getPassphrase() == null
        && PairingVersion.V1_0_0.equals(pairingPayload.getPairing().getVersion())) {
      pairingPayload.getPairing().setPassphrase(true);
    }
    pairingPayload.validate();
    return pairingPayload;
  }

  protected void validate() throws NotifiableException {
    // main validation
    try {
      super.validate();
    } catch (Exception e) {
      throw new NotifiableException(e.getMessage());
    }

    // whirlpool validation
    if (!PairingType.WHIRLPOOL_GUI.equals(getPairing().getType())) {
      throw new NotifiableException("Unsupported pairing.type");
    }
    if (!PairingVersion.V1_0_0.equals(getPairing().getVersion())
        && !PairingVersion.V2_0_0.equals(getPairing().getVersion())
        && !PairingVersion.V3_0_0.equals(getPairing().getVersion())) {
      throw new NotifiableException("Unsupported pairing.version");
    }
    if (getPairing().getPassphrase() == null) {
      throw new NotifiableException("Invalid pairing.passphrase");
    }
  }
}

package com.samourai.whirlpool.cli.api.controllers.rest.wallet;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.ricochet.Ricochet;
import com.samourai.wallet.ricochet.RicochetConfig;
import com.samourai.wallet.ricochet.RicochetUtilGeneric;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.spend.SpendBuilder;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.TxUtil;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSpendPreviewRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSpendPreviewResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSpendRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSpendResponse;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class SpendController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;
  @Autowired private CliConfig cliConfig;
  private RicochetUtilGeneric ricochetUtil = RicochetUtilGeneric.getInstance();

  @RequestMapping(value = CliApiEndpoint.REST_WALLET_SPEND_PREVIEW, method = RequestMethod.POST)
  public ApiSpendPreviewResponse preview(
      @Valid @RequestBody ApiSpendPreviewRequest payload, @RequestHeader HttpHeaders headers)
      throws Exception {
    checkHeaders(headers);

    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    if (payload.ricochet != null) {
      // preview ricochet
      Ricochet ricochet = previewRicochet(payload, whirlpoolWallet);
      return new ApiSpendPreviewResponse(ricochet, whirlpoolWallet.getUtxoSupplier());
    }

    SpendBuilder spendBuilder = whirlpoolWallet.getSpendBuilder();

    try {
      // preview
      SpendTx spendTx = computeSpendTx(spendBuilder, whirlpoolWallet, payload);
      return new ApiSpendPreviewResponse(spendTx, whirlpoolWallet.getUtxoSupplier());
    } catch (SpendException e) {
      // forward SpendError
      throw new NotifiableException(e.getSpendError().name());
    }
  }

  protected Ricochet previewRicochet(
      ApiSpendPreviewRequest payload, WhirlpoolWallet whirlpoolWallet) throws Exception {
    if (payload.ricochet.hops < RicochetUtilGeneric.defaultNbHops) {
      throw new NotifiableException("Invalid ricochet.hops");
    }
    int feePerB = computeFeePerB(payload, whirlpoolWallet);
    RicochetConfig config =
        whirlpoolWallet.newRicochetConfig(feePerB, payload.ricochet.useTimeLock, payload.account);
    return ricochetUtil.ricochet(payload.spendAmount, payload.spendTo, config);
  }

  protected int computeFeePerB(ApiSpendPreviewRequest payload, WhirlpoolWallet whirlpoolWallet) {
    return payload.minerFeeSatPerByte != null
        ? payload.minerFeeSatPerByte
        : whirlpoolWallet.getMinerFeeSupplier().getFee(payload.minerFeeTarget);
  }

  @RequestMapping(value = CliApiEndpoint.REST_WALLET_SPEND, method = RequestMethod.POST)
  public ApiSpendResponse spend(
      @Valid @RequestBody ApiSpendRequest payload, @RequestHeader HttpHeaders headers)
      throws Exception {
    checkHeaders(headers);

    CliWallet cliWallet = cliWalletService.getSessionWallet();

    // check passphrase
    if (!cliWallet.checkPassphraseHash(payload.passphraseHash)) {
      throw new NotifiableException("Invalid passphrase");
    }

    try {
      if (payload.ricochet != null) {
        // preview ricochet
        Ricochet ricochet = previewRicochet(payload, cliWallet);

        // broadcast ricochet
        String lastTxId = null;
        for (String txHex : ricochet.getTransactions()) {
          lastTxId = cliWallet.pushTx(txHex);
        }
        return new ApiSpendResponse(lastTxId, ricochet, cliWallet.getUtxoSupplier());
      }

      // preview
      SpendBuilder spendBuilder = cliWallet.getSpendBuilder();

      SpendTx spendTx = computeSpendTx(spendBuilder, cliWallet, payload);

      // spend
      Transaction tx = spendTx.getTx();
      cliWallet.pushTx(TxUtil.getInstance().getTxHex(tx));

      return new ApiSpendResponse(tx.getHashAsString(), spendTx, cliWallet.getUtxoSupplier());
    } catch (SpendException e) {
      // forward SpendError
      throw new NotifiableException(e.getSpendError().name());
    }
  }

  private SpendTx computeSpendTx(
      SpendBuilder spendBuilder, WhirlpoolWallet whirlpoolWallet, ApiSpendPreviewRequest payload)
      throws Exception {
    int feePerB = computeFeePerB(payload, whirlpoolWallet);
    BigInteger feePerKb = FeeUtil.getInstance().toFeePerKB(feePerB);

    NetworkParameters params = cliConfig.getServer().getParams();
    List<MyTransactionOutPoint> preselectedInputs = null;
    if (payload.spendFrom != null && payload.spendFrom.size() > 0) {
      preselectedInputs = new LinkedList<>();
      for (ApiUtxoRef utxo : payload.spendFrom) {
        WhirlpoolUtxo whirlpoolUtxo =
            whirlpoolWallet.getUtxoSupplier().findUtxo(utxo.hash, utxo.index);
        if (whirlpoolUtxo == null) {
          throw new NotifiableException("Utxo not found: " + utxo.toString());
        }
        preselectedInputs.add(whirlpoolUtxo.getUtxo().computeOutpoint(params));
      }
    }

    int blockHeight = whirlpoolWallet.getChainSupplier().getLatestBlock().height;
    // TODO zeroleak
    BipWallet spendWallet =
        whirlpoolWallet.getWalletSupplier().getWallet(payload.account, BIP_FORMAT.SEGWIT_NATIVE);
    BipWallet changeWallet = spendWallet;
    SpendTx spendTx =
        spendBuilder.preview(
            spendWallet,
            changeWallet,
            payload.spendTo,
            payload.spendAmount,
            payload.stonewall,
            payload.rbfOptIn,
            feePerKb,
            null,
            preselectedInputs,
            blockHeight);
    return spendTx;
  }
}

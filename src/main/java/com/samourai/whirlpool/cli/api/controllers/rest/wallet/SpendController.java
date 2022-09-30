package com.samourai.whirlpool.cli.api.controllers.rest.wallet;

import com.samourai.soroban.cahoots.CahootsContext;
import com.samourai.soroban.client.wallet.sender.SorobanWalletInitiator;
import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.cahoots.Cahoots;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.ricochet.Ricochet;
import com.samourai.wallet.ricochet.RicochetConfig;
import com.samourai.wallet.ricochet.RicochetUtilGeneric;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.spend.SpendBuilder;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
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
import com.samourai.whirlpool.client.wallet.beans.SamouraiAccountIndex;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class SpendController extends AbstractRestController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
          lastTxId = cliWallet.getPushTx().pushTx(txHex);
        }
        return new ApiSpendResponse(lastTxId, ricochet, cliWallet.getUtxoSupplier());
      }

      SpendTx spendTx = null;
      if (payload.cahoots != null) {
        // do Cahoots & broadcast
        spendTx = doCahoots(payload, cliWallet);
      }
      if (spendTx == null) {
        // preview
        SpendBuilder spendBuilder = cliWallet.getSpendBuilder();
        spendTx = computeSpendTx(spendBuilder, cliWallet, payload);

        // broadcast
        Transaction tx = spendTx.getTx();
        cliWallet.getPushTx().pushTx(TxUtil.getInstance().getTxHex(tx));
      }
      return new ApiSpendResponse(spendTx, cliWallet.getUtxoSupplier());
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

  private SpendTx doCahoots(ApiSpendRequest request, CliWallet cliWallet) throws Exception {
    int account = SamouraiAccountIndex.find(request.account);
    CahootsWallet cahootsWallet = cliWallet.getCahootsWallet();

    int feePerB = computeFeePerB(request, cliWallet);
    PaymentCode paymentCodeCounterparty = new PaymentCode(request.cahoots.paymentCodeCounterparty);

    CahootsType cahootsType = CahootsType.valueOf(request.cahoots.cahootsType);
    if (CahootsType.MULTI.equals(cahootsType)) {
      boolean isTestnet =
          FormatsUtilGeneric.getInstance().isTestNet(cliConfig.getServer().getParams());
      paymentCodeCounterparty = new PaymentCode(SamouraiWalletConst.getSaasPcode(isTestnet));
    }

    // initiate cahoots
    CahootsContext cahootsContext =
        CahootsContext.newInitiator(
            cahootsWallet,
            cahootsType,
            account,
            feePerB,
            request.spendAmount,
            request.spendTo,
            null);
    SorobanWalletInitiator sorobanWalletInitiator = cliWalletService.getSorobanWalletInitiator();
    Cahoots cahoots =
        AsyncUtil.getInstance()
            .blockingGet(
                sorobanWalletInitiator.meetAndInitiate(cahootsContext, paymentCodeCounterparty));

    // push
    cahoots.pushTx(cliWallet.getPushTx());
    return cahoots.getSpendTx(cahootsContext, cliWallet.getUtxoSupplier());
  }
}

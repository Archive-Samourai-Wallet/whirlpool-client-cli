package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.wallet.api.backend.ISweepBackend;
import com.samourai.wallet.api.backend.MinerFeeTarget;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.send.SweepUtilGeneric;
import com.samourai.wallet.send.beans.SweepPreview;
import com.samourai.wallet.util.PrivKeyReader;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSweepPreviewRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSweepPreviewResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiSweepSubmitRequest;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import java.util.Collection;
import javax.validation.Valid;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class SweepController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;
  @Autowired private SweepUtilGeneric sweepUtil;
  @Autowired private BipFormatSupplier bipFormatSupplier;

  @RequestMapping(value = CliApiEndpoint.REST_SWEEP_PREVIEW, method = RequestMethod.POST)
  public ApiSweepPreviewResponse sweepPreview(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiSweepPreviewRequest payload)
      throws Exception {
    checkHeaders(headers);

    // preview sweep
    Collection<SweepPreview> sweepPreviews = doSweepPreview(payload);
    return new ApiSweepPreviewResponse(sweepPreviews);
  }

  @RequestMapping(value = CliApiEndpoint.REST_SWEEP_SUBMIT, method = RequestMethod.POST)
  public void sweepSubmit(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiSweepSubmitRequest payload)
      throws Exception {
    checkHeaders(headers);

    // preview sweep
    Collection<SweepPreview> sweepPreviews = doSweepPreview(payload);

    // submit
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    long blockHeight = whirlpoolWallet.getChainSupplier().getLatestBlock().height;
    for (SweepPreview sweepPreview : sweepPreviews) {
      String receiveAddress = whirlpoolWallet.getDepositAddress(true);
      sweepUtil.sweep(
          sweepPreview, receiveAddress, getSweepBackend(), bipFormatSupplier, true, blockHeight);
    }
  }

  protected ISweepBackend getSweepBackend() throws Exception {
    return cliWalletService.getSessionWallet().getSweepBackend();
  }

  protected Collection<SweepPreview> doSweepPreview(ApiSweepPreviewRequest payload)
      throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    NetworkParameters params = whirlpoolWallet.getConfig().getNetworkParameters();

    // sweep preview
    PrivKeyReader privKeyReader = new PrivKeyReader(payload.privateKey, params);
    long feePerB = whirlpoolWallet.getMinerFeeSupplier().getFee(MinerFeeTarget.BLOCKS_4);
    return sweepUtil.sweepPreviews(privKeyReader, feePerB, getSweepBackend());
  }
}

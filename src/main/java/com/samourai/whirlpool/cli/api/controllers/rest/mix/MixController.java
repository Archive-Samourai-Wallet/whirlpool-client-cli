package com.samourai.whirlpool.cli.api.controllers.rest.mix;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiMixHistoryResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiWalletStateResponse;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.MixHistory;
import com.samourai.whirlpool.client.wallet.beans.MixingState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MixController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;

  @RequestMapping(value = CliApiEndpoint.REST_MIX, method = RequestMethod.GET)
  public ApiWalletStateResponse mix(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    MixingState mixingState = whirlpoolWallet.getMixingState();
    MixHistory mixHistory = whirlpoolWallet.getMixHistory();
    int latestBlockHeight = whirlpoolWallet.getChainSupplier().getLatestBlock().height;
    return new ApiWalletStateResponse(mixingState, mixHistory, latestBlockHeight);
  }

  @RequestMapping(value = CliApiEndpoint.REST_MIX_HISTORY, method = RequestMethod.GET)
  public ApiMixHistoryResponse mixHistory(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    MixHistory mixHistory = whirlpoolWallet.getMixHistory();
    ApiMixHistoryResponse response = new ApiMixHistoryResponse(mixHistory.getMixResultsDesc());
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_MIX_HISTORY_EXTERNAL_XPUB, method = RequestMethod.GET)
  public ApiMixHistoryResponse mixHistoryExternalXpub(@RequestHeader HttpHeaders headers)
      throws Exception {
    checkHeaders(headers);

    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    MixHistory mixHistory = whirlpoolWallet.getMixHistory();
    ApiMixHistoryResponse response =
        new ApiMixHistoryResponse(mixHistory.getMixResultsExternalXpubDesc());
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_MIX_START, method = RequestMethod.POST)
  public void start(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);
    cliWalletService.getSessionWallet().startAsync().blockingAwait();
  }

  @RequestMapping(value = CliApiEndpoint.REST_MIX_STOP, method = RequestMethod.POST)
  public void stop(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);
    cliWalletService.getSessionWallet().stop();
  }
}

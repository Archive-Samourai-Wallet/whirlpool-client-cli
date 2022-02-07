package com.samourai.whirlpool.cli.api.controllers.rest.mix;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiWalletStateResponse;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
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
  public ApiWalletStateResponse wallet(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    MixingState mixingState = whirlpoolWallet.getMixingState();
    int latestBlockHeight = whirlpoolWallet.getChainSupplier().getLatestBlock().height;
    return new ApiWalletStateResponse(mixingState, latestBlockHeight);
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

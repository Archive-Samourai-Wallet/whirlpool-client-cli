package com.samourai.whirlpool.cli.api.controllers.rest.utxo;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiWalletUtxosResponse;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class UtxosListController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;

  @RequestMapping(value = CliApiEndpoint.REST_UTXOS, method = RequestMethod.GET)
  public ApiWalletUtxosResponse wallet(
      @RequestHeader HttpHeaders headers,
      @RequestParam(value = "refresh", defaultValue = "false") boolean refresh)
      throws Exception {
    checkHeaders(headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    if (refresh) {
      // wait for utxo refresh
      whirlpoolWallet.refreshUtxosAsync().blockingAwait();
    }
    return new ApiWalletUtxosResponse(whirlpoolWallet);
  }
}

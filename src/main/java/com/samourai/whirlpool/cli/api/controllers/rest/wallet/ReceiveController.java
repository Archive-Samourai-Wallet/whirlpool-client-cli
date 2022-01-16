package com.samourai.whirlpool.cli.api.controllers.rest.wallet;

import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiDepositResponse;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReceiveController extends AbstractRestController {
  @Autowired private CliWalletService cliWalletService;

  @Deprecated
  @RequestMapping(value = CliApiEndpoint.REST_WALLET_DEPOSIT, method = RequestMethod.GET)
  public ApiDepositResponse deposit(
      @RequestParam(value = "increment", defaultValue = "false") boolean increment,
      @RequestHeader HttpHeaders headers)
      throws Exception {
    checkHeaders(headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    String depositAddress = whirlpoolWallet.getDepositAddress(increment);
    return new ApiDepositResponse(depositAddress);
  }
}

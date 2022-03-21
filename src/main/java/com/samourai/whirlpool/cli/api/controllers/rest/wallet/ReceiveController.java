package com.samourai.whirlpool.cli.api.controllers.rest.wallet;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiDepositResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiReceiveRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiReceiveResponse;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import javax.validation.Valid;
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

  @RequestMapping(value = CliApiEndpoint.REST_WALLET_RECEIVE, method = RequestMethod.POST)
  public ApiReceiveResponse receive(
      @Valid @RequestBody ApiReceiveRequest payload, @RequestHeader HttpHeaders headers)
      throws Exception {
    checkHeaders(headers);

    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    BipFormatSupplier bipFormatSupplier = whirlpoolWallet.getUtxoSupplier().getBipFormatSupplier();
    BipFormat bipFormat = null;
    if (payload.bipFormat != null) {
      bipFormat = bipFormatSupplier.findById(payload.bipFormat);
    }
    if (bipFormat == null) {
      bipFormat = BIP_FORMAT.SEGWIT_NATIVE;
    }

    BipWallet receiveWallet =
        whirlpoolWallet.getWalletSupplier().getWallet(payload.account, bipFormat);
    BipAddress bipAddress = receiveWallet.getNextAddress(payload.increment);
    return new ApiReceiveResponse(bipFormat, bipAddress);
  }
}

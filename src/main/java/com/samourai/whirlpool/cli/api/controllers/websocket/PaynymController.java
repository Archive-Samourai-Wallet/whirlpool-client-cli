package com.samourai.whirlpool.cli.api.controllers.websocket;

import com.samourai.wallet.util.AsyncUtil;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPaynymFollowRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPaynymResponse;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiPaynymUnfollowRequest;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.services.WSMessageService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.data.paynym.PaynymSupplier;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PaynymController extends AbstractWebSocketController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliWalletService cliWalletService;

  @Autowired
  public PaynymController(WSMessageService wsMessageService, CliWalletService cliWalletService) {
    super(wsMessageService);
    this.cliWalletService = cliWalletService;
  }

  @MessageMapping(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_PAYNYM_REFRESH)
  public ApiPaynymResponse paynymRefresh(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    logMessage(principal, headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    PaynymSupplier paynymSupplier = whirlpoolWallet.getPaynymSupplier();

    // refresh paynym
    paynymSupplier.refresh();
    return new ApiPaynymResponse(paynymSupplier.getPaynymState());
  }

  @MessageMapping(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_PAYNYM_FOLLOW)
  public void paynymFollow(
      Principal principal,
      StompHeaderAccessor headers,
      @Valid @RequestBody ApiPaynymFollowRequest request)
      throws Exception {
    logMessage(principal, headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    PaynymSupplier paynymSupplier = whirlpoolWallet.getPaynymSupplier();

    // follow
    AsyncUtil.getInstance().blockingAwait(paynymSupplier.follow(request.paymentCodeTarget));
  }

  @MessageMapping(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_PAYNYM_UNFOLLOW)
  public void paynymUnfollow(
      Principal principal,
      StompHeaderAccessor headers,
      @Valid @RequestBody ApiPaynymUnfollowRequest request)
      throws Exception {
    logMessage(principal, headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    PaynymSupplier paynymSupplier = whirlpoolWallet.getPaynymSupplier();

    // unfollow
    AsyncUtil.getInstance().blockingAwait(paynymSupplier.unfollow(request.paymentCodeTarget));
  }

  @MessageMapping(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_PAYNYM_CLAIM)
  public void claim(Principal principal, StompHeaderAccessor headers) throws Exception {
    logMessage(principal, headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    PaynymSupplier paynymSupplier = whirlpoolWallet.getPaynymSupplier();

    // claim
    AsyncUtil.getInstance().blockingAwait(paynymSupplier.claim());
  }

  @MessageExceptionHandler
  public void handleException(Exception exception, Principal principal) {
    super.handleException(exception, principal);
  }
}

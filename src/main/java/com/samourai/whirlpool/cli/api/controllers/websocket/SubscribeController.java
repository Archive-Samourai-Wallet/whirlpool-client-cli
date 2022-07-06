package com.samourai.whirlpool.cli.api.controllers.websocket;

import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.services.WSMessageService;
import com.samourai.whirlpool.cli.services.WsNotifierService;
import com.samourai.whirlpool.protocol.WhirlpoolProtocol;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class SubscribeController extends AbstractWebSocketController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private WsNotifierService wsNotifierService;

  @Autowired
  public SubscribeController(
      WSMessageService wsMessageService, WsNotifierService wsNotifierService) {
    super(wsMessageService);
    this.wsNotifierService = wsNotifierService;
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_USER_PRIVATE + WhirlpoolProtocol.WS_PREFIX_USER_REPLY)
  public void subcribeUserReply(Principal principal, StompHeaderAccessor headers) throws Exception {
    onSubscribe(principal, headers);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_WALLET)
  public ApiWalletDataResponse subscribeWallet(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.walletData(null);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_STATE)
  public ApiCliStateResponse subscribeState(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.state();
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_POOLS)
  public ApiPoolsResponse subscribePools(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.pools(null);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_MIX_STATE)
  public ApiMixStateResponse subscribeMixState(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.mixState(null);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_CHAIN)
  public ApiChainDataResponse subscribeChain(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.chainData(null);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_MINER_FEE)
  public ApiMinerFeeResponse subscribeMinerFee(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.minerFee(null);
  }

  @SubscribeMapping(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_PAYNYM)
  public ApiPaynymResponse subscribePaynym(Principal principal, StompHeaderAccessor headers)
      throws Exception {
    onSubscribe(principal, headers);
    return wsNotifierService.paynym(null);
  }

  private void onSubscribe(Principal principal, StompHeaderAccessor headers) {
    // don't validate headers here, so user is able to receive protocol version mismatch errors
    logMessage(principal, headers);
  }

  @MessageExceptionHandler
  public void handleException(Exception exception, Principal principal) {
    super.handleException(exception, principal);
  }
}

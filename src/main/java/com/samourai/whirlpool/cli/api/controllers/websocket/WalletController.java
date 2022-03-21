package com.samourai.whirlpool.cli.api.controllers.websocket;

import com.samourai.wallet.util.AsyncUtil;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.services.WSMessageService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WalletController extends AbstractWebSocketController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliWalletService cliWalletService;

  @Autowired
  public WalletController(WSMessageService wsMessageService, CliWalletService cliWalletService) {
    super(wsMessageService);
    this.cliWalletService = cliWalletService;
  }

  @MessageMapping(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_WALLET_REFRESH)
  public void walletRefresh(Principal principal, StompHeaderAccessor headers) throws Exception {
    logMessage(principal, headers);
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();

    // refresh utxos
    AsyncUtil.getInstance().blockingAwait(whirlpoolWallet.refreshUtxosAsync());
  }

  @MessageExceptionHandler
  public void handleException(Exception exception, Principal principal) {
    super.handleException(exception, principal);
  }
}

package com.samourai.whirlpool.cli.api.controllers.websocket;

import com.samourai.whirlpool.cli.services.WSMessageService;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public abstract class AbstractWebSocketController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String VERSION_HEADER = "version";
  private static final String VERSION_CURRENT = "1.0";

  private WSMessageService wsMessageService;

  public AbstractWebSocketController(WSMessageService WSMessageService) {
    this.wsMessageService = WSMessageService;
  }

  protected void validateHeaders(StompHeaderAccessor headers) throws Exception {
    String clientVersion = headers.getFirstNativeHeader(VERSION_HEADER);
    if (!VERSION_CURRENT.equals(clientVersion)) {
      throw new NotifiableException(
          "Version mismatch: expected="
              + VERSION_CURRENT
              + ", client="
              + (clientVersion != null ? clientVersion : "unknown"));
    }
  }

  protected void logMessage(Principal principal, StompHeaderAccessor headers) {
    // don't validate headers here, so user is able to receive protocol version mismatch errors
    String username = principal.getName();
    if (log.isDebugEnabled()) {
      log.debug("(<) [" + username + "] > " + headers.getDestination());
    }
  }

  protected void handleException(Exception e, Principal principal) {
    NotifiableException notifiable = NotifiableException.computeNotifiableException(e);
    String message = notifiable.getMessage();
    String username = principal.getName();
    wsMessageService.sendPrivateError(username, message);
  }

  protected WSMessageService getWsMessageService() {
    return wsMessageService;
  }
}

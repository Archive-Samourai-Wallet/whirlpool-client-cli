package com.samourai.whirlpool.cli.services;

import com.samourai.javawsserver.config.JWSSConfig;
import com.samourai.javawsserver.services.JWSSMessageService;
import com.samourai.whirlpool.protocol.websocket.messages.ErrorResponse;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WSMessageService extends JWSSMessageService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  public WSMessageService(
      SimpMessagingTemplate messagingTemplate, TaskExecutor taskExecutor, JWSSConfig config) {
    super(messagingTemplate, taskExecutor, config);
  }

  public void sendPrivateError(String username, String message) {
    log.warn("(>) " + username + " sendPrivateError: " + message);
    ErrorResponse errorResponse = new ErrorResponse(message);
    sendPrivate(username, errorResponse);
  }
}

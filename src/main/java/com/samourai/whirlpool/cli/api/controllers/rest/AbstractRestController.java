package com.samourai.whirlpool.cli.api.controllers.rest;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.samourai.whirlpool.cli.CliApplication;
import com.samourai.whirlpool.cli.api.protocol.CliApi;
import com.samourai.whirlpool.cli.utils.AuthUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;

public abstract class AbstractRestController {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired private TaskExecutor taskExecutor;

  public AbstractRestController() {}

  protected void checkHeaders(HttpHeaders httpHeaders) throws Exception {
    // check apiVersion
    String requestApiVersion = httpHeaders.getFirst(CliApi.HEADER_API_VERSION);
    if (!Strings.isEmpty(requestApiVersion) && !CliApi.API_VERSION.equals(requestApiVersion)) {
      throw new NotifiableException(
          "API version mismatch: requestVersion="
              + requestApiVersion
              + ", cliVersion="
              + CliApi.API_VERSION);
    }

    if (isAuthRequired()) {
      // check accessToken
      String accessToken = getAccessToken(httpHeaders);
      verifyToken(accessToken);
    }
  }

  protected boolean isAuthRequired() {
    // override this to allow anonymous access to controllers
    return true;
  }

  protected String getAccessToken(HttpHeaders httpHeaders) {
    String accessToken = null;

    String auth = httpHeaders.getFirst(CliApi.HEADER_AUTH);
    if (!Strings.isEmpty(auth)) {
      String[] token = auth.split(" "); // split "Bearer"
      if (token.length > 1) {
        accessToken = token[1]; // get token string
      }
    }
    return accessToken;
  }

  // throws exception if expired
  protected void verifyToken(String accessToken) throws Exception {
    if (Strings.isEmpty(accessToken)) {
      throw new Exception("Access Token required");
    }
    try {
      AuthUtils.verifyToken(accessToken);
    } catch (JWTVerificationException e) {
      throw new Exception("Invalid Access Token: " + accessToken + " " + e.getMessage());
    }
  }

  protected void restartAfterReply() {
    // restart CLI *AFTER* response reply
    taskExecutor.execute(
        () -> {
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
            log.error("", e);
          }
          CliApplication.restart();
        });
  }
}

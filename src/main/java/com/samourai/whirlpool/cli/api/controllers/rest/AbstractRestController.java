package com.samourai.whirlpool.cli.api.controllers.rest;

import com.samourai.whirlpool.cli.CliApplication;
import com.samourai.whirlpool.cli.api.protocol.CliApi;
import com.samourai.whirlpool.cli.config.CliConfig;
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

  @Autowired private CliConfig cliConfig;
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

    // check apiKey
    if (!Strings.isEmpty(cliConfig.getApiKey())) {
      String requestApiKey = httpHeaders.getFirst(CliApi.HEADER_API_KEY);
      if (!cliConfig.getApiKey().equals(requestApiKey)) {
        throw new NotifiableException("API key rejected: " + requestApiKey);
      }
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

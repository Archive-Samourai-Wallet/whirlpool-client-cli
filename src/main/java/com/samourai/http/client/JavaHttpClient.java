package com.samourai.http.client;

import com.samourai.whirlpool.client.utils.ClientUtils;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaHttpClient extends JettyHttpClient {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private HttpUsage httpUsage;

  public JavaHttpClient(
      long requestTimeout, java.util.Optional<HttpProxy> cliProxyOptional, HttpUsage httpUsage) {
    super(requestTimeout, cliProxyOptional, ClientUtils.USER_AGENT);
    log = ClientUtils.prefixLogger(log, httpUsage.name());
    this.httpUsage = httpUsage;
  }

  public HttpUsage getHttpUsage() {
    return httpUsage;
  }
}

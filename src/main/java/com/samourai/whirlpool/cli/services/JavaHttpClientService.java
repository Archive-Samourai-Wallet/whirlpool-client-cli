package com.samourai.whirlpool.cli.services;

import com.samourai.http.client.HttpProxy;
import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.IHttpClientService;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.whirlpool.cli.config.CliConfig;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaHttpClientService implements IHttpClientService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliTorClientService torClientService;
  private CliConfig cliConfig;

  private Map<HttpUsage, JavaHttpClient> httpClients;

  public JavaHttpClientService(CliTorClientService torClientService, CliConfig cliConfig) {
    this.torClientService = torClientService;
    this.cliConfig = cliConfig;
    this.httpClients = new ConcurrentHashMap<>();
  }

  @Override
  public JavaHttpClient getHttpClient(HttpUsage httpUsage) {
    JavaHttpClient httpClient = httpClients.get(httpUsage);
    if (httpClient == null) {
      if (log.isDebugEnabled()) {
        log.debug("+httpClient[" + httpUsage + "]");
      }
      httpClient = computeHttpClient(httpUsage);
      httpClients.put(httpUsage, httpClient);
    }
    return httpClient;
  }

  private JavaHttpClient computeHttpClient(HttpUsage httpUsage) {
    // use Tor proxy if any
    Optional<HttpProxy> cliProxy = torClientService.getTorProxy(httpUsage);
    // or default proxy
    if (!cliProxy.isPresent()) {
      cliProxy = cliConfig.getCliProxy();
    }
    return new JavaHttpClient(cliConfig.getRequestTimeout(), cliProxy, httpUsage);
  }

  public void changeIdentityRest() {
    for (JavaHttpClient httpClient : httpClients.values()) {
      // restart REST clients
      if (httpClient.getHttpUsage().isRest()) {
        if (httpClient != null) {
          httpClient.restart();
        }
      }
    }
    // don't break non-REST connexions, it will be renewed on next connexion
  }

  @Override
  public void stop() {
    for (JavaHttpClient httpClient : httpClients.values()) {
      httpClient.stop();
    }
  }
}

package com.samourai.whirlpool.cli.services;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.IHttpClient;
import com.samourai.http.client.IHttpClientService;
import com.samourai.stomp.client.JettyStompClientService;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.protocol.WhirlpoolProtocol;
import org.springframework.stereotype.Service;

@Service
public class JavaStompClientService extends JettyStompClientService {

  public JavaStompClientService(JavaHttpClientService httpClientService) {
    super(
        adaptHttpClientService(httpClientService),
        WhirlpoolProtocol.HEADER_MESSAGE_TYPE,
        ClientUtils.USER_AGENT);
  }

  private static IHttpClientService adaptHttpClientService(
      JavaHttpClientService httpClientService) {
    return new IHttpClientService() {
      @Override
      public IHttpClient getHttpClient() {
        return httpClientService.getHttpClient(HttpUsage.COORDINATOR_WEBSOCKET);
      }

      @Override
      public void stop() {
        httpClientService.stop();
      }
    };
  }
}

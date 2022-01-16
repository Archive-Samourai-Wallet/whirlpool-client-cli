package com.samourai.websocket.client;

import com.samourai.http.client.HttpUsage;
import com.samourai.whirlpool.cli.services.JavaHttpClientService;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaWebsocketClient implements IWebsocketClient {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private WebSocketClient wsClient;
  private Session session;
  private IWebsocketClientListener listener;

  public JavaWebsocketClient(JavaHttpClientService httpClientService) throws Exception {
    HttpClient jettyHttpClient =
        httpClientService.getHttpClient(HttpUsage.BACKEND).getJettyHttpClient();
    this.wsClient = new WebSocketClient(jettyHttpClient);
  }

  @Override
  public synchronized void connect(String url, IWebsocketClientListener listener) throws Exception {
    if (this.session != null) {
      if (log.isDebugEnabled()) {
        log.debug("already connected");
      }
      return;
    }
    this.listener = listener;
    wsClient.start();
    wsClient.connect(computeWebSocketListener(), URI.create(url)).get();
  }

  @Override
  public void send(String payload) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(" -> " + payload);
    }
    session.getRemote().sendString(payload);
  }

  @Override
  public synchronized void disconnect() {
    try {
      wsClient.stop();
    } catch (Exception e) {
    }
    this.session = null;
  }

  private WebSocketListener computeWebSocketListener() {
    return new WebSocketListener() {
      @Override
      public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        // ok
      }

      @Override
      public void onWebSocketText(String msg) {
        if (listener != null) {
          listener.onMessage(msg);
        } else {
          log.warn(" <- (IGNORED) {}", msg);
        }
      }

      @Override
      public void onWebSocketClose(int code, String reason) {
        if (listener != null) {
          listener.onClose(reason);
        } else {
          log.warn("ws: onClose (IGNORED) {}", reason);
        }
      }

      @Override
      public void onWebSocketConnect(Session session) {
        JavaWebsocketClient.this.session = session;
        if (listener != null) {
          listener.onConnect();
        } else {
          log.warn("ws: onConnect (IGNORED)");
        }
      }

      @Override
      public void onWebSocketError(Throwable cause) {
        if (listener != null) {
          listener.onClose(cause.getMessage());
        } else {
          log.warn("ws: onError (IGNORED) {}", cause.getMessage());
        }
      }
    };
  }
}

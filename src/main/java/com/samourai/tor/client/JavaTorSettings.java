package com.samourai.tor.client;

import com.msopentech.thali.toronionproxy.DefaultSettings;
import com.samourai.http.client.HttpProxy;
import com.samourai.http.client.HttpProxyProtocol;
import java.util.Optional;

public class JavaTorSettings extends DefaultSettings {
  private HttpProxy cliProxy;
  private String customTorrc;

  public JavaTorSettings(Optional<HttpProxy> cliProxy, String customTorrc) {
    this.cliProxy = cliProxy.orElse(null);
    this.customTorrc = customTorrc;
  }

  @Override
  public String getCustomTorrc() {
    return customTorrc;
  }

  @Override
  public String dnsPort() {
    return "auto";
  }

  @Override
  public int getHttpTunnelPort() {
    return 0;
  }

  @Override
  public int getRelayPort() {
    return 0;
  }

  @Override
  public String getSocksPort() {
    return "auto";
  }

  @Override
  public String transPort() {
    // not available on mac
    return "0";
  }

  @Override
  public boolean runAsDaemon() {
    return false;
  }

  @Override
  public boolean hasSafeSocks() {
    // remote DNS resolving is not supported by Java
    return false;
  }

  @Override
  public String getProxyHost() {
    if (cliProxy != null && HttpProxyProtocol.HTTP.equals(cliProxy.getProtocol())) {
      return cliProxy.getHost();
    }
    return null;
  }

  @Override
  public String getProxyPort() {
    if (cliProxy != null && HttpProxyProtocol.HTTP.equals(cliProxy.getProtocol())) {
      return Integer.toString(cliProxy.getPort());
    }
    return null;
  }

  @Override
  public String getProxySocks5Host() {
    if (cliProxy != null && HttpProxyProtocol.SOCKS.equals(cliProxy.getProtocol())) {
      return cliProxy.getHost();
    }
    return null;
  }

  @Override
  public String getProxySocks5ServerPort() {
    if (cliProxy != null && HttpProxyProtocol.SOCKS.equals(cliProxy.getProtocol())) {
      return Integer.toString(cliProxy.getPort());
    }
    return null;
  }

  @Override
  public boolean disableNetwork() {
    return false;
  }

  @Override
  public String toString() {
    return "TorSettings[proxy=" + (cliProxy != null ? cliProxy : "null") + "]";
  }
}

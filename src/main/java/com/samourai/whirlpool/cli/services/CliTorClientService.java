package com.samourai.whirlpool.cli.services;

import com.samourai.http.client.IHttpProxySupplier;
import com.samourai.tor.client.JavaTorClient;
import com.samourai.wallet.httpClient.HttpProxy;
import com.samourai.wallet.httpClient.HttpUsage;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.client.exception.NotifiableException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliTorClientService implements IHttpProxySupplier {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Optional<JavaTorClient> torClient;
  private CliConfig cliConfig;
  private Collection<HttpUsage> torHttpUsages;

  public CliTorClientService(CliConfig cliConfig) {
    super();
    this.torClient = Optional.empty();
    this.cliConfig = cliConfig;
    this.torHttpUsages = cliConfig.computeTorHttpUsages();
  }

  public void setup() throws Exception {
    if (cliConfig.getTor()) {
      if (!torClient.isPresent()) {
        if (log.isDebugEnabled()) {
          log.debug("Enabling Tor for: " + torHttpUsages);
        }
        // instanciate & initialize
        JavaTorClient tc = new JavaTorClient(cliConfig, torHttpUsages); // throws
        torClient = Optional.of(tc);
        if (log.isDebugEnabled()) {
          log.debug("Tor is enabled.");
        }
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Tor is disabled.");
      }
    }
  }

  public void connect() {
    if (torClient.isPresent()) {
      torClient.get().connect();
    }
  }

  public void waitReady() throws NotifiableException {
    if (torClient.isPresent()) {
      torClient.get().waitReady();
    }
  }

  public void shutdown() {
    if (torClient.isPresent()) {
      torClient.get().shutdown();
    }
  }

  @Override
  public void changeIdentity() {
    if (torClient.isPresent()) {
      torClient.get().changeIdentity();
    }
  }

  @Override
  public Optional<HttpProxy> getHttpProxy(HttpUsage httpUsage) {
    boolean isTorUsage = torHttpUsages.contains(httpUsage);
    if (isTorUsage && torClient.isPresent()) {
      // use Tor proxy
      return torClient.get().getTorProxy(httpUsage);
    }
    // use default proxy
    return cliConfig.getCliProxy();
  }

  public Optional<Integer> getProgress() {
    if (!torClient.isPresent()) {
      if (cliConfig.getTor()) {
        return Optional.of(0); // Tor is initializing
      }
      return Optional.empty(); // Tor is disabled
    }

    int progress = torClient.get().getProgress();
    return Optional.of(progress);
  }
}

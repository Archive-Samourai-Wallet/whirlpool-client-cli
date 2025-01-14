package com.samourai.whirlpool.cli.config;

import com.samourai.soroban.client.SorobanConfig;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.constants.SamouraiNetwork;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.util.ExtLibJConfig;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import com.samourai.whirlpool.cli.services.JavaHttpClientService;
import java.lang.invoke.MethodHandles;
import java.security.Provider;
import org.apache.catalina.connector.Connector;
import org.bitcoinj.core.NetworkParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableCaching
public class CliServicesConfig {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public CliServicesConfig() {}

  @Bean
  TaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean
  Bech32UtilGeneric bech32Util() {
    return Bech32UtilGeneric.getInstance();
  }

  @Bean
  SamouraiNetwork samouraiNetwork(CliConfig cliConfig) {
    return cliConfig.getSamouraiNetwork();
  }

  @Bean
  NetworkParameters networkParameters(SamouraiNetwork samouraiNetwork) {
    return samouraiNetwork.getParams();
  }

  @Bean
  BipFormatSupplier bipFormatSupplier() {
    return BIP_FORMAT.PROVIDER;
  }

  @Bean
  SorobanConfig sorobanConfig(CliConfig cliConfig, JavaHttpClientService httpClientService) {
    boolean torOnionSoroban =
        cliConfig.getTor()
            && cliConfig.getTorConfig().getSoroban().isEnabled()
            && cliConfig.getTorConfig().getSoroban().isOnion();
    Provider provider = new BouncyCastleProvider();
    ExtLibJConfig extLibJConfig =
        new ExtLibJConfig(
            cliConfig.getSamouraiNetwork(), torOnionSoroban, provider, httpClientService);
    return new SorobanConfig(extLibJConfig);
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("OPTIONS");
    config.addAllowedMethod("HEAD");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedMethod("PATCH");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  @ConditionalOnProperty(name = ApiCliConfig.KEY_API_HTTP_ENABLE, havingValue = "true")
  public ServletWebServerFactory httpServer(CliConfig cliConfig) {
    // https not required => configure HTTP server
    int httpPort = cliConfig.getApi().getHttpPort();
    if (log.isDebugEnabled()) {
      log.debug("Enabling API over HTTP... httpPort=" + httpPort);
    }
    Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
    connector.setPort(httpPort);
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    factory.addAdditionalTomcatConnectors(connector);
    return factory;
  }
}

package com.samourai.whirlpool.cli.config;

import com.samourai.http.client.IHttpClient;
import com.samourai.http.client.IHttpClientService;
import com.samourai.javawsserver.config.JWSSConfig;
import com.samourai.soroban.client.rpc.RpcService;
import com.samourai.soroban.client.wallet.SorobanWalletService;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.crypto.CryptoUtil;
import com.samourai.wallet.payload.PayloadUtilGeneric;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.send.SweepUtilGeneric;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import com.samourai.whirlpool.cli.utils.WalletRoutingDataSource;
import com.samourai.whirlpool.client.exception.NotifiableException;
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
  PayloadUtilGeneric payloadUtil() {
    return PayloadUtilGeneric.getInstance();
  }

  @Bean
  SweepUtilGeneric sweepUtil() {
    return SweepUtilGeneric.getInstance();
  }

  @Bean
  NetworkParameters networkParameters(CliConfig cliConfig) {
    return cliConfig.getServer().getParams();
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

  @Bean
  JWSSConfig jwssConfig() {
    return new JWSSConfig(
        CliApiEndpoint.WS_ENDPOINTS,
        CliApiEndpoint.WS_PREFIX_USER_PRIVATE,
        CliApiEndpoint.WS_PREFIX,
        CliApiEndpoint.WS_PREFIX_DESTINATION,
        CliApiEndpoint.WS_PREFIX_USER_REPLY);
  }

  @Bean
  WalletRoutingDataSource walletRoutingDataSource() throws NotifiableException {
    return new WalletRoutingDataSource();
  }

  @Bean
  BipFormatSupplier bipFormatSupplier() {
    return BIP_FORMAT.PROVIDER;
  }

  @Bean
  BIP47UtilGeneric bip47Util() {
    return Bip47UtilJava.getInstance();
  }

  // SOROBAN

  @Bean
  CryptoUtil cryptoUtil() {
    Provider provider = new BouncyCastleProvider();
    return CryptoUtil.getInstance(provider);
  }

  @Bean
  RpcService rpcService(
      IHttpClientService httpClientService, CryptoUtil cryptoUtil, CliConfig cliConfig) {
    IHttpClient httpClient = httpClientService.getHttpClient();
    return new RpcService(httpClient, cryptoUtil, cliConfig.useOnionCoordinator());
  }

  @Bean
  SorobanWalletService sorobanWalletService(
      BIP47UtilGeneric bip47Util,
      BipFormatSupplier bipFormatSupplier,
      NetworkParameters params,
      RpcService rpcService) {
    return new SorobanWalletService(bip47Util, bipFormatSupplier, params, rpcService);
  }
}

package com.samourai.whirlpool.cli.config;

import com.samourai.http.client.IHttpClientService;
import com.samourai.soroban.client.rpc.RpcClientService;
import com.samourai.soroban.client.wallet.SorobanWalletService;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.bip47.rpc.java.SecretPointFactoryJava;
import com.samourai.wallet.bip47.rpc.secretPoint.ISecretPointFactory;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.crypto.CryptoUtil;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolNetwork;
import java.lang.invoke.MethodHandles;
import org.apache.catalina.connector.Connector;
import org.bitcoinj.core.NetworkParameters;
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
  // reproductible testMode identity:
  // PM8TJRuNYsxKEjQGC3DEDoTYmDfCijUEZHe2gt7RitgZ17AnX7DybtH7Zzw1ZoJDQtNzxm8mvQLhQrZnpjdATzRRGMhNX2W8esNkHoHs3Sq4kh9aUG1m
  private static final String TESTMODE_SEED_WORDS =
      "all all all all all all all all all all all all";
  private static final String TESTMODE_SEED_PASSPHRASE = "all";

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
  WhirlpoolNetwork whirlpoolNetwork(CliConfig cliConfig) {
    return cliConfig.getServer().getWhirlpoolNetwork();
  }

  @Bean
  NetworkParameters networkParameters(WhirlpoolNetwork whirlpoolNetwork) {
    return whirlpoolNetwork.getParams();
  }

  @Bean
  ISecretPointFactory secretPointFactory() {
    return SecretPointFactoryJava.getInstance();
  }

  @Bean
  CryptoUtil cryptoUtil() {
    return CryptoUtil.getInstanceJava();
  }

  @Bean
  BIP47UtilGeneric bip47UtilGeneric() {
    return Bip47UtilJava.getInstance();
  }

  @Bean
  BipFormatSupplier bipFormatSupplier() {
    return BIP_FORMAT.PROVIDER;
  }

  @Bean
  RpcClientService rpcClientService(
      IHttpClientService httpClientService,
      CryptoUtil cryptoUtil,
      BIP47UtilGeneric bip47Util,
      CliConfig cliConfig,
      NetworkParameters params) {
    boolean onion =
        cliConfig.getTor()
            && cliConfig.getTorConfig().getSoroban().isEnabled()
            && cliConfig.getTorConfig().getSoroban().isOnion();
    RpcClientService rpcClientService =
        new RpcClientService(httpClientService, cryptoUtil, bip47Util, onion, params);
    if (cliConfig.getMix().isTestMode()) {
      // for reproductible tests
      try {
        HD_Wallet hdw =
            HD_WalletFactoryGeneric.getInstance()
                .restoreWalletFromWords(TESTMODE_SEED_WORDS, TESTMODE_SEED_PASSPHRASE, params);
        BIP47Wallet bip47Wallet = new BIP47Wallet(hdw);
        BIP47Account bip47Account = bip47Wallet.getAccount(0);
        rpcClientService._setTestModeBIP47Account(bip47Account);
      } catch (Exception e) {
        log.error("", e);
      }
    }
    return rpcClientService;
  }

  @Bean
  SorobanWalletService sorobanWalletService(
      BIP47UtilGeneric bip47Util,
      BipFormatSupplier bipFormatSupplier,
      NetworkParameters params,
      RpcClientService rpcClientService) {
    return new SorobanWalletService(bip47Util, bipFormatSupplier, params, rpcClientService);
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

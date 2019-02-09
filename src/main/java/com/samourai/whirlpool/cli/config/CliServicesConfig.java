package com.samourai.whirlpool.cli.config;

import com.samourai.http.client.IHttpClient;
import com.samourai.wallet.hd.java.HD_WalletFactoryJava;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.whirlpool.cli.ApplicationArgs;
import com.samourai.whirlpool.cli.services.CliPushTxService;
import com.samourai.whirlpool.cli.services.JavaStompClientService;
import com.samourai.whirlpool.cli.services.SamouraiApiService;
import com.samourai.whirlpool.client.WhirlpoolClient;
import com.samourai.whirlpool.client.tx0.Tx0Service;
import com.samourai.whirlpool.client.wallet.pushTx.PushTxService;
import com.samourai.whirlpool.client.whirlpool.WhirlpoolClientConfig;
import com.samourai.whirlpool.client.whirlpool.WhirlpoolClientImpl;
import java.lang.invoke.MethodHandles;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
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

  protected CliConfig whirlpoolCliConfig;

  public CliServicesConfig(CliConfig whirlpoolCliConfig) {
    this.whirlpoolCliConfig = whirlpoolCliConfig;
  }

  @Bean
  ApplicationArgs applicationArgs(ApplicationArguments applicationArguments) {
    ApplicationArgs appArgs = new ApplicationArgs(applicationArguments);

    // override configuration file with cli args
    appArgs.override(whirlpoolCliConfig);

    return appArgs;
  }

  @Bean
  TaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean
  HD_WalletFactoryJava hdWalletFactory() {
    return HD_WalletFactoryJava.getInstance();
  }

  @Bean
  Bech32UtilGeneric bech32Util() {
    return Bech32UtilGeneric.getInstance();
  }

  @Bean
  NetworkParameters networkParameters(CliConfig cliConfig) {
    return cliConfig.getNetworkParameters();
  }

  @Bean
  WhirlpoolClient whirlpoolClient(WhirlpoolClientConfig whirlpoolClientConfig) {
    return WhirlpoolClientImpl.newClient(whirlpoolClientConfig);
  }

  @Bean
  WhirlpoolClientConfig whirlpoolClientConfig(
      CliConfig cliConfig, IHttpClient httpClient, JavaStompClientService javaStompClientService) {
    WhirlpoolClientConfig config =
        new WhirlpoolClientConfig(
            httpClient,
            javaStompClientService,
            cliConfig.getServer().getUrl(),
            cliConfig.getNetworkParameters());
    config.setSsl(cliConfig.getServer().isSsl());

    String scode = cliConfig.getScode();
    if (!StringUtils.isEmpty(scode)) {
      config.setScode(scode);
    }
    return config;
  }

  @Bean
  PushTxService pushTxService(CliConfig cliConfig, SamouraiApiService samouraiApiService) {
    return new CliPushTxService(cliConfig, samouraiApiService);
  }

  @Bean
  Tx0Service tx0Service(CliConfig cliConfig) {
    return new Tx0Service(
        cliConfig.getNetworkParameters(),
        cliConfig.getFee().getXpub(),
        cliConfig.getFee().getValue());
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
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
}

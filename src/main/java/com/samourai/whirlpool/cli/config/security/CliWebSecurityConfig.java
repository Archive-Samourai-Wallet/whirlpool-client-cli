package com.samourai.whirlpool.cli.config.security;

import com.samourai.javawsserver.config.JWSSConfig;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.config.CliConfig;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class CliWebSecurityConfig extends WebSecurityConfigurerAdapter {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CliConfig cliConfig;
  private JWSSConfig config;

  @Autowired
  public CliWebSecurityConfig(CliConfig cliConfig, JWSSConfig config) {
    this.cliConfig = cliConfig;
    this.config = config;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    boolean httpEnable = cliConfig.getApi().isHttpEnable();
    if (log.isDebugEnabled()) {
      log.debug("Configuring REST API: httpEnable=" + httpEnable);
    }

    // allow frames for h2-console
    // http.headers().frameOptions().disable();

    // disable CSRF
    http.csrf()
        .disable()

        // authorize REST API
        .authorizeRequests()
        .antMatchers(CliApiEndpoint.REST_ENDPOINTS)
        .permitAll()

        // authorize websocket API
        .antMatchers(CliApiEndpoint.WS_PREFIX + CliApiEndpoint.WS_CONNECT + "/**")
        .permitAll()

        // .antMatchers("/h2-console/**").permitAll()

        // reject others
        .anyRequest()
        .denyAll();

    if (!httpEnable) {
      http.requiresChannel().anyRequest().requiresSecure();
    }
  }
}

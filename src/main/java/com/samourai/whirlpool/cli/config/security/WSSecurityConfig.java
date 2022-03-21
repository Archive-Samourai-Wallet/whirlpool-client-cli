package com.samourai.whirlpool.cli.config.security;

import com.samourai.javawsserver.config.JWSSConfig;
import com.samourai.javawsserver.config.JWSSWebSocketSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WSSecurityConfig extends JWSSWebSocketSecurityConfig {

  @Autowired
  public WSSecurityConfig(JWSSConfig config) {
    super(config);
  }
}

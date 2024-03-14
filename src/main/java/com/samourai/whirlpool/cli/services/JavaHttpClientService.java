package com.samourai.whirlpool.cli.services;

import com.samourai.http.client.JettyHttpClientService;
import com.samourai.whirlpool.cli.config.CliConfig;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaHttpClientService extends JettyHttpClientService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public JavaHttpClientService(CliTorClientService torClientService, CliConfig cliConfig) {
    super(cliConfig.getRequestTimeout(), torClientService);
  }
}

package com.samourai.whirlpool.client.test;

import com.samourai.http.client.JettyHttpClient;
import com.samourai.wallet.httpClient.IHttpClient;
import com.samourai.whirlpool.cli.utils.CliUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootConfiguration
@ActiveProfiles(CliUtils.SPRING_PROFILE_TESTING)
@ExtendWith(SpringExtension.class) // required for injecting BuildProperties
@Import({BuildProperties.class})
public class AbstractTest {
  protected static final long requestTimeout = 5000;
  protected IHttpClient httpClient;

  @BeforeEach
  public void setup() throws Exception {
    // LogbackUtils.setLogLevel("root", Level.DEBUG.toString());
    CliUtils.setLogLevel(true, false);

    httpClient = new JettyHttpClient(requestTimeout, null, null);
  }

  @AfterEach
  public void tearDown() throws Exception {}
}

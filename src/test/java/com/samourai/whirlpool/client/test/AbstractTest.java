package com.samourai.whirlpool.client.test;

import ch.qos.logback.classic.Level;
import com.samourai.http.client.IWhirlpoolHttpClientService;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.lang.invoke.MethodHandles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
  protected static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AsyncUtil asyncUtil = AsyncUtil.getInstance();
  @Autowired protected IWhirlpoolHttpClientService httpClientService;
  @Autowired protected CliConfig cliConfig;

  public AbstractTest() {
    ClientUtils.setLogLevel(Level.DEBUG, Level.DEBUG);
  }

  @BeforeEach
  public void setup() throws Exception {
    // LogbackUtils.setLogLevel("root", Level.DEBUG.toString());
    CliUtils.setLogLevel(true, false);
  }

  @AfterEach
  public void tearDown() throws Exception {}
}

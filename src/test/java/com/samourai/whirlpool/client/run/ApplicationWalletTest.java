package com.samourai.whirlpool.client.run;

import com.samourai.whirlpool.client.Application;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationWalletTest extends AbstractApplicationTest {
  private static final String SEED_WORDS =
      "hub casual home drift winter such economy wage waste wagon essay torch";
  private static final String SEED_PASSPHRASE = "whirlpool";
  protected static final String SERVER = "127.0.0.1:8080";
  protected static final String RPC_CLIENT_URL = "http://user:password@host:port";

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void runLoopWallet() {
    String[] args =
        new String[] {
          "--network=test",
          "--seed-passphrase=" + SEED_PASSPHRASE,
          "--rpc-client-url=" + RPC_CLIENT_URL,
          "--seed-words=" + SEED_WORDS,
          "--debug",
          "--pool=0.01btc",
          "--test-mode",
          "--server=" + SERVER,
        };
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);
    new Application().run(appArgs);

    Assert.assertTrue(getErr().isEmpty());
  }

  @Test
  public void runTx0WithRpcClientUrl() {
    String[] args =
        new String[] {
          "--network=test",
          "--seed-passphrase=" + SEED_PASSPHRASE,
          "--rpc-client-url=" + RPC_CLIENT_URL,
          "--seed-words=" + SEED_WORDS,
          "--tx0=20",
          "--debug",
          "--pool=0.01btc",
          "--server=" + SERVER
        };
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);
    new Application().run(appArgs);

    Assert.assertTrue(getErr().isEmpty());
  }

  @Test
  public void runTx0WithoutRpcClientUrl() {
    String[] args =
        new String[] {
          "--network=test",
          "--seed-passphrase=" + SEED_PASSPHRASE,
          "--seed-words=" + SEED_WORDS,
          "--tx0=20",
          "--debug",
          "--pool=0.01btc",
          "--server=" + SERVER
        };
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);
    new Application().run(appArgs);

    Assert.assertTrue(getErr().isEmpty());
  }

  @Test
  public void runAggregatePostmix() {
    String[] args =
        new String[] {
          "--network=test",
          "--seed-passphrase=" + SEED_PASSPHRASE,
          "--rpc-client-url=" + RPC_CLIENT_URL,
          "--seed-words=" + SEED_WORDS,
          "--aggregate-postmix",
          "--debug",
          "--pool=0.01btc",
          "--server=" + SERVER
        };
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);
    new Application().run(appArgs);

    Assert.assertTrue(getErr().isEmpty());
  }
}

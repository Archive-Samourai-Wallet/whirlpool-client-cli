package com.samourai.whirlpool.client.run;

import com.samourai.whirlpool.cli.CliApplication;
import org.junit.jupiter.api.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled
public class CliApplicationTest extends AbstractApplicationTest {

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void runListPools() {
    String[] args = new String[] {"--debug"};
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);

    new CliApplication().run(appArgs);

    Assertions.assertTrue(getOut().contains(" • Fetching pools..."));
    Assertions.assertTrue(getErr().isEmpty());
  }

  @Test
  public void runApp() {
    String[] args =
        new String[] {
          "--listen",
          "--authenticate",
          "--debug-client",
          "--debug",
          "--clients=5",
          "--auto-tx0=0.01btc",
          "--tx0-max-outputs=15"
        };
    CliApplication.main(args);
    while (true) {
      try {
        Thread.sleep(100000);
      } catch (InterruptedException e) {
      }
    }
  }

  @Test
  public void runWhirlpool() {
    String[] args =
        new String[] {
          "--utxo=733a1bcb4145e3dd0ea3e6709bef9504fd252c9a26b254508539e3636db659c2-1",
          "--utxo-key=cUe6J7Fs5mxg6jLwXE27xcDpaTPXfQZ9oKDbxs5PP6EpYMFHab2T",
          "--utxo-balance=1000102",
          "--seed-passphrase=w0",
          "--seed-words=all all all all all all all all all all all all",
          "--mixs=5",
          "--debug",
          "--test-mode"
        };
    ApplicationArguments appArgs = new DefaultApplicationArguments(args);

    captureSystem();
    new CliApplication().run(appArgs); // TODO mock server
    resetSystem();

    Assertions.assertTrue(getOut().contains(" • connecting to "));
    Assertions.assertTrue(getErr().isEmpty());
  }
}

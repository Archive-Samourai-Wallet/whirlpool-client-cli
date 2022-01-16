package com.samourai.whirlpool.cli.services;

import com.samourai.whirlpool.client.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CliWalletServiceTest extends AbstractTest {
  @Autowired private CliWalletService cliWalletService;

  @Override
  public void setup() throws Exception {
    super.setup();
  }

  @Test
  public void decryptSeedWords() throws Exception {
    String seedWordsEncrypted;
    String passphrase;

    seedWordsEncrypted =
        "t6MNj4oCb9T54lKWNAF274Hg72E0q0uJooUwKjzGD+ysWsFv8Ib47ubdnjStkeJ/G9UltiERHAm1tKRtHbaJiA==";
    passphrase = "secret";
    Assertions.assertTrue(
        cliWalletService.decryptSeedWords(seedWordsEncrypted, passphrase).split(" ").length == 12);
  }
}

package com.samourai.whirlpool.cli.utils;

import com.samourai.whirlpool.cli.beans.Encrypted;
import java.lang.invoke.MethodHandles;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptUtilsTest {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String SERIALIZED =
      "mD37gOLQe2BVzOe0OATJGF3oTcCzjkNplCBj/IxN8IucuRN55CL1Wolk6noorpFQGTJtUl4MB/W7WxypUSDeZZKAkCpqMe2VkajaONNKIfydkwWhrRFmL6J5iBI0sR92zqhWmIywfrCZjWNWz5I+yv/GizMeu/xZ8apAswftp6r+tSo=";
  private static final String IV = "mD37gOLQe2BVzOe0OATJGA==";
  private static final String SALT = "XehNwLOOQ2k=";
  private static final String CT =
      "lCBj/IxN8IucuRN55CL1Wolk6noorpFQGTJtUl4MB/W7WxypUSDeZZKAkCpqMe2VkajaONNKIfydkwWhrRFmL6J5iBI0sR92zqhWmIywfrCZjWNWz5I+yv/GizMeu/xZ8apAswftp6r+tSo=";

  private static final String KEY = "test";
  private static final String PLAIN =
      "abandon abandon abandon abandon abandon abandon abandon abandon abandon ability absorb acid";

  @Test
  public void encrypt() throws Exception {
    Encrypted encrypted = EncryptUtils.encrypt(KEY, PLAIN);

    String decrypted = EncryptUtils.decrypt(KEY, encrypted);

    Assertions.assertEquals(PLAIN, decrypted);
  }

  @Test
  public void decrypt() throws Exception {
    String iv = IV;
    String salt = SALT;
    String ct = CT;
    Encrypted encrypted = new Encrypted(iv, salt, ct);
    String decryptedSeedWords = EncryptUtils.decrypt(KEY, encrypted);

    Assertions.assertEquals(PLAIN, decryptedSeedWords);
  }

  @Test
  public void serializeEncrypted() throws Exception {
    String iv = IV;
    String salt = SALT;
    String ct = CT;
    Encrypted encrypted = new Encrypted(iv, salt, ct);

    String serializeEncrypted = EncryptUtils.serializeEncrypted(encrypted);

    Assertions.assertEquals(SERIALIZED, serializeEncrypted);

    Encrypted encryptedBis = EncryptUtils.unserializeEncrypted(serializeEncrypted);
    Assertions.assertArrayEquals(encrypted.getIv(), encryptedBis.getIv());
    Assertions.assertArrayEquals(encrypted.getSalt(), encryptedBis.getSalt());
    Assertions.assertArrayEquals(encrypted.getCt(), encryptedBis.getCt());
  }

  @Test
  public void unserializeEncrypted() throws Exception {
    Encrypted encrypted = EncryptUtils.unserializeEncrypted(SERIALIZED);

    // expected
    byte[] iv = Base64.decode(IV);
    byte[] salt = Base64.decode(SALT);
    byte[] ct = Base64.decode(CT);

    Assertions.assertArrayEquals(iv, encrypted.getIv());
    Assertions.assertArrayEquals(salt, encrypted.getSalt());
    Assertions.assertArrayEquals(ct, encrypted.getCt());

    String serializedBis = EncryptUtils.serializeEncrypted(encrypted);
    Assertions.assertEquals(SERIALIZED, serializedBis);
  }
}

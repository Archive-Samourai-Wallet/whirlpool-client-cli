package com.samourai.whirlpool.cli.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthUtils {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int SECRET_LENGTH = 64;
  private static final String SECRET = CliUtils.generateRandomString(SECRET_LENGTH);
  private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

  public static DecodedJWT verifyToken(String token) throws Exception {
    JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
    DecodedJWT jwt = JWT.decode(token);
    return verifier.verify(token);
  }

  public static String createToken(Date expireAt) throws Exception {
    if (expireAt.before(new Date())) {
      throw new Exception("Trying to create Token with already expired Date: " + expireAt);
    }

    return JWT.create().withIssuer("auth0").withExpiresAt(expireAt).sign(algorithm);
  }
}

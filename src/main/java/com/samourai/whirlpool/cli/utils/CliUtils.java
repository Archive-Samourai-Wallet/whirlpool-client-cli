package com.samourai.whirlpool.cli.utils;

import ch.qos.logback.classic.Level;
import com.samourai.http.client.HttpProxy;
import com.samourai.http.client.HttpProxyProtocol;
import com.samourai.whirlpool.cli.exception.NoUserInputException;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.utils.LogbackUtils;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.token.Sha512DigestUtils;

public class CliUtils {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String LOG_SEPARATOR = "⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿";
  public static final String SPRING_PROFILE_TESTING = "testing";

  public static String sha512Hash(String str) {
    return Sha512DigestUtils.shaHex(str);
  }

  public static void waitUserAction(String message) throws NotifiableException {
    Console console = getConsole();
    if (console != null) {
      log.info("⣿ ACTION REQUIRED ⣿ " + message);
      log.info("Press <ENTER> when ready:");
      console.readLine();
    } else {
      throw new NotifiableException("⣿ ACTION REQUIRED ⣿ " + message);
    }
  }

  public static String readUserInputRequired(
      String message, boolean secret, String[] allowedValues) {
    message = "⣿ INPUT REQUIRED ⣿ " + message;
    String input;
    do {
      input = readUserInput(message, secret);
    } while (input == null || !ArrayUtils.contains(allowedValues, input));
    return input;
  }

  public static boolean readUserInputRequiredBoolean(String message) {
    String input =
        CliUtils.readUserInputRequired(
            message + " (y/n)", false, new String[] {"y", "n", "Y", "N"});
    return input.toLowerCase().equals("y");
  }

  public static int readUserInputRequiredInt(String message, int minValue) {
    return readUserInputRequiredInt(message, minValue, null);
  }

  public static int readUserInputRequiredInt(String message, int minValue, Integer defaultValue) {
    while (true) {
      String input = CliUtils.readUserInput(message, false);
      // default value
      if (defaultValue != null && StringUtils.isEmpty(input)) {
        return defaultValue;
      }
      // numeric constraints
      try {
        int inputNumeric = Integer.parseInt(input);
        if (inputNumeric >= minValue) {
          return inputNumeric;
        }
      } catch (Exception e) {
      }
    }
  }

  public static String readUserInputRequired(String message, boolean secret) {
    message = "⣿ INPUT REQUIRED ⣿ " + message;
    String input;
    do {
      input = readUserInput(message, secret);
    } while (input == null);
    return input;
  }

  public static String readUserInputRequiredMinLength(
      String message, boolean secret, int minLength) {
    message = "⣿ INPUT REQUIRED ⣿ " + message;
    String input;
    do {
      input = readUserInput(message, secret);
      if (input != null && input.length() < minLength) {
        System.out.println("Min length: " + minLength);
        input = null;
      }
    } while (input == null);
    return input;
  }

  public static File readFileName(String message) {
    do {
      String input = readUserInputRequired(message, false);
      try {
        File f = new File(input);
        if (f.exists() && f.isFile()) {
          return f;
        }
      } catch (Exception e) {
      }
      System.out.println("File not found: " + input);
    } while (true);
  }

  public static String readUserInput(String message, boolean secret) {
    Console console = getConsole();
    String inviteMessage = message + ">";

    // read line
    String line = null;
    if (console != null) {
      console.printf(inviteMessage);
      line = secret ? new String(console.readPassword()) : console.readLine();
    } else {
      // allow console redirection
      Scanner input = getConsoleRedirection();
      System.out.print(inviteMessage);
      if (!input.hasNextLine()) {
        throw new NoUserInputException();
      }
      line = input.nextLine();
    }
    if (line != null) {
      line = line.trim();
      if (line.isEmpty()) {
        line = null;
      }
    }
    return line;
  }

  public static Character readChar() {
    Console console = getConsole();
    if (console != null) {
      try {
        return (char) console.reader().read();
      } catch (IOException e) {
        return null;
      }
    }
    return null;
  }

  public static Scanner getConsoleRedirection() {
    Scanner input = new Scanner(System.in);
    return input;
  }

  public static Console getConsole() {
    return System.console();
  }

  public static boolean hasConsole() {
    return getConsole() != null;
  }

  public static void notifyError(String message) {
    log.error("⣿ ERROR ⣿ " + message);
  }

  public static Optional<HttpProxy> computeProxy(final String proxy) {
    if (StringUtils.isEmpty(proxy)) {
      return Optional.empty();
    }
    String[] splitProtocol = proxy.split("://");
    if (splitProtocol.length != 2) {
      throw new IllegalArgumentException("Invalid proxy: " + proxy);
    }
    HttpProxyProtocol proxyProtocol =
        HttpProxyProtocol.find(splitProtocol[0].toUpperCase())
            .orElseThrow(
                () -> new IllegalArgumentException("Unsupported proxy protocol: " + proxy));
    String[] split = splitProtocol[1].split(":");
    if (split.length != 2) {
      throw new IllegalArgumentException("Invalid proxy: " + proxy);
    }
    try {
      int port = Integer.parseInt(split[1]);
      if (port < 1) {
        throw new IllegalArgumentException("Invalid proxy port: " + proxy);
      }
      String host = split[0];
      if (StringUtils.isEmpty(host)) {
        throw new IllegalArgumentException("Invalid proxy host: " + proxy);
      }
      return Optional.of(new HttpProxy(proxyProtocol, host, port));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid proxy: " + proxy);
    }
  }

  public static void useProxy(HttpProxy cliProxy) {
    String portStr = Integer.toString(cliProxy.getPort()); // important cast
    switch (cliProxy.getProtocol()) {
      case SOCKS:
        System.setProperty("socksProxyHost", cliProxy.getHost());
        System.setProperty("socksProxyPort", portStr);
        break;
      case HTTP:
        System.setProperty("http.proxyHost", cliProxy.getHost());
        System.setProperty("http.proxyPort", portStr);
        System.setProperty("https.proxyHost", cliProxy.getHost());
        System.setProperty("https.proxyPort", portStr);
        break;
    }
  }

  public static List<String> execOrEmpty(String cmd) throws Exception {
    try {
      return exec(cmd);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(
            "execOrNull error: "
                + e.getClass().getName()
                + ": "
                + (e.getMessage() != null ? e.getMessage() : ""));
      }
    }
    return new ArrayList<>();
  }

  public static List<String> exec(String cmd) throws Exception {
    List<String> lines = new ArrayList<>();
    Process proc = null;
    Scanner scanner = null;
    try {
      proc = Runtime.getRuntime().exec(cmd);

      scanner = new Scanner(proc.getInputStream());
      while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine());
      }

      int exit = proc.waitFor();
      if (exit != 0) {
        String output = StringUtils.join(lines, "\n");
        throw new RuntimeException(
            "exec [" + cmd + "] returned error code: " + exit + "\nOutput:\n" + output);
      }
    } finally {
      if (proc != null) {
        proc.destroy();
      }
      if (scanner != null) {
        scanner.close();
      }
    }
    return lines;
  }

  public static void setLogLevel(boolean isDebug, boolean isDebugClient) {
    Level whirlpoolLevel = isDebug ? (isDebugClient ? Level.TRACE : Level.DEBUG) : Level.INFO;
    Level whirlpoolClientLevel = isDebugClient ? Level.TRACE : Level.INFO;
    ClientUtils.setLogLevel(whirlpoolLevel, whirlpoolClientLevel);

    LogbackUtils.setLogLevel("com.samourai.whirlpool.cli", whirlpoolLevel.toString());
    LogbackUtils.setLogLevel(
        "com.msopentech.thali.toronionproxy", org.slf4j.event.Level.WARN.toString());
    LogbackUtils.setLogLevel(
        "com.msopentech.thali.java.toronionproxy", org.slf4j.event.Level.WARN.toString());
    LogbackUtils.setLogLevel("org.springframework.web", org.slf4j.event.Level.INFO.toString());
    LogbackUtils.setLogLevel("org.springframework.test", org.slf4j.event.Level.INFO.toString());
    LogbackUtils.setLogLevel("org.apache.http.impl.conn", org.slf4j.event.Level.INFO.toString());
    LogbackUtils.setLogLevel("org.eclipse.jetty", org.slf4j.event.Level.WARN.toString());
  }
}

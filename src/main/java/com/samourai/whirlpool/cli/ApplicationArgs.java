package com.samourai.whirlpool.cli;

import com.samourai.whirlpool.cli.config.CliConfig;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

/** Parsing command-line client arguments. */
@Service
public class ApplicationArgs {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String ARG_DEBUG = "debug";
  private static final String ARG_DEBUG_CLIENT = "debug-client";
  public static final String ARG_LIST_POOLS = "list-pools";
  private static final String ARG_SCODE = "scode";
  private static final String ARG_CLIENTS = "clients";
  private static final String ARG_CLIENT_DELAY = "client-delay";
  private static final String ARG_TX0_DELAY = "tx0-delay";
  private static final String ARG_TX0_MAX_OUTPUTS = "tx0-max-outputs";
  public static final String ARG_AGGREGATE = "aggregate";
  private static final String ARG_AUTO_TX0_AGGREGATE = "auto-tx0-aggregate";
  private static final String ARG_AUTO_TX0 = "auto-tx0";
  private static final String ARG_AUTO_MIX = "auto-mix";
  private static final String ARG_LISTEN = "listen";
  private static final String ARG_API_KEY = "api-key";
  public static final String ARG_INIT = "init";
  public static final String ARG_SET_EXTERNAL_XPUB = "set-external-xpub";
  private static final String ARG_AUTHENTICATE = "authenticate";
  public static final String ARG_DUMP_PAYLOAD = "dump-payload";
  private static final String ARG_RESYNC = "resync";

  private ApplicationArguments args;

  public ApplicationArgs(ApplicationArguments args) {
    this.args = args;
  }

  public void override(CliConfig cliConfig) {
    String value;
    Boolean valueBool;
    Integer valueInt;

    value = optionalOption(ARG_SCODE);
    if (value != null) {
      cliConfig.setScode(value);
    }

    valueBool = optionalBoolean(ARG_AUTO_MIX);
    if (valueBool != null) {
      cliConfig.getMix().setAutoMix(valueBool);
    }

    valueInt = optionalInt(ARG_CLIENTS);
    if (valueInt != null) {
      cliConfig.getMix().setClients(valueInt);
    }

    valueInt = optionalInt(ARG_CLIENT_DELAY);
    if (valueInt != null) {
      cliConfig.getMix().setClientDelay(valueInt);
    }

    valueInt = optionalInt(ARG_TX0_DELAY);
    if (valueInt != null) {
      cliConfig.getMix().setAutoTx0Delay(valueInt);
    }

    valueInt = optionalInt(ARG_TX0_MAX_OUTPUTS);
    if (valueInt != null) {
      cliConfig.getMix().setTx0MaxOutputs(valueInt);
    }

    valueBool = optionalBoolean(ARG_AUTO_TX0_AGGREGATE);
    if (valueBool != null) {
      cliConfig.setAutoTx0Aggregate(valueBool);
    }

    value = optionalOption(ARG_AUTO_TX0);
    if (value != null) {
      cliConfig.setAutoTx0PoolId(value);
    }

    value = optionalOption(ARG_API_KEY);
    if (value != null) {
      cliConfig.setApiKey(value);
    }

    valueBool = optionalBoolean(ARG_RESYNC);
    if (valueBool != null) {
      cliConfig.setResync(valueBool);
    }
  }

  public boolean isListPools() {
    Boolean listPools = optionalBoolean(ARG_LIST_POOLS);
    if (listPools == null) {
      listPools = false;
    }
    return listPools;
  }

  public boolean isDumpPayload() {
    return args.containsOption(ARG_DUMP_PAYLOAD);
  }

  public boolean isResync() {
    return args.containsOption(ARG_RESYNC);
  }

  public String getAggregate() {
    return optionalOption(ARG_AGGREGATE);
  }

  public boolean isAggregate() {
    return !StringUtils.isEmpty(getAggregate());
  }

  public boolean isInit() {
    return args.containsOption(ARG_INIT);
  }

  public boolean isSetExternalXpub() {
    return args.containsOption(ARG_SET_EXTERNAL_XPUB);
  }

  public boolean isAuthenticate() {
    return args.containsOption(ARG_AUTHENTICATE);
  }

  public static boolean getMainListen(String[] mainArgs) {
    return mainBoolean(ARG_LISTEN, mainArgs);
  }

  public static boolean isMainDebug(String[] mainArgs) {
    return mainBoolean(ARG_DEBUG, mainArgs);
  }

  public static boolean isMainDebugClient(String[] mainArgs) {
    return mainBoolean(ARG_DEBUG_CLIENT, mainArgs);
  }

  private String requireOption(String name, String defaultValue) {
    // arg not found
    if (!args.getOptionNames().contains(name)) {
      if (log.isDebugEnabled()) {
        log.debug("--" + name + "=" + defaultValue + " (default value)");
      }
      return defaultValue;
    }
    // --param (with no value) => "true"
    Iterator<String> iter = args.getOptionValues(name).iterator();
    if (!iter.hasNext()) {
      return "true";
    }
    return iter.next();
  }

  private String requireOption(String name) {
    String value = requireOption(name, null);
    if (value == null) {
      throw new IllegalArgumentException("Missing required option: " + name);
    }
    return value;
  }

  private String optionalOption(String name) {
    return requireOption(name, null);
  }

  private Boolean optionalBoolean(String name) {
    String value = optionalOption(name);
    return value != null ? Boolean.parseBoolean(value) : null;
  }

  private Integer optionalInt(String name) {
    String value = optionalOption(name);
    return value != null ? Integer.parseInt(value) : null;
  }

  private static String mainArg(
      String name,
      String[] mainArgs,
      String defaultValueWhenNotPresent,
      String defaultValueWhenPresent) {
    Optional<String> argFound =
        Stream.of(mainArgs).filter(s -> s.startsWith("--" + name)).findFirst();

    // arg not found
    if (!argFound.isPresent()) {
      return defaultValueWhenNotPresent;
    }

    // --param (with no value) => "true"
    String[] argSplit = argFound.get().split("=");
    if (argSplit.length == 1) {
      return defaultValueWhenPresent;
    }
    return argSplit[1];
  }

  private static Boolean mainBoolean(String name, String[] mainArgs) {
    return Boolean.parseBoolean(mainArg(name, mainArgs, "false", "true"));
  }

  private static Integer mainInteger(
      String name,
      String[] mainArgs,
      Integer defaultValueWhenNotPresent,
      Integer defaultValueWhenPresent) {
    String str =
        mainArg(
            name,
            mainArgs,
            defaultValueWhenNotPresent != null
                ? Integer.toString(defaultValueWhenNotPresent)
                : null,
            defaultValueWhenPresent != null ? Integer.toString(defaultValueWhenPresent) : null);
    if (str == null) {
      return null;
    }
    return Integer.parseInt(str);
  }

  public ApplicationArguments getApplicationArguments() {
    return args;
  }
}

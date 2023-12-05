package com.samourai.whirlpool.cli.api.protocol;

public class CliApiEndpoint {
  public static final String REST_PREFIX = "/rest/";

  public static final String REST_CLI = REST_PREFIX + "cli";
  public static final String REST_CLI_INIT = REST_PREFIX + "cli/init";
  public static final String REST_CLI_LOGIN = REST_PREFIX + "cli/login";
  public static final String REST_CLI_LOGOUT = REST_PREFIX + "cli/logout";
  public static final String REST_CLI_RESTART = REST_PREFIX + "cli/restart";
  public static final String REST_CLI_RESYNC = REST_PREFIX + "cli/resync";
  public static final String REST_CLI_CONFIG = REST_PREFIX + "cli/config";

  public static final String REST_POOLS = REST_PREFIX + "pools";

  public static final String REST_WALLET_DEPOSIT = REST_PREFIX + "wallet/deposit";

  public static final String REST_MIX = REST_PREFIX + "mix";
  public static final String REST_MIX_HISTORY = REST_PREFIX + "mix/history";
  public static final String REST_MIX_HISTORY_EXTERNAL_XPUB =
      REST_PREFIX + "mix/history/externalXpub";
  public static final String REST_MIX_START = REST_PREFIX + "mix/start";
  public static final String REST_MIX_STOP = REST_PREFIX + "mix/stop";

  public static final String REST_UTXOS = REST_PREFIX + "utxos";
  public static final String REST_UTXO_STARTMIX = REST_PREFIX + "utxos/{hash}:{index}/startMix";
  public static final String REST_UTXO_STOPMIX = REST_PREFIX + "utxos/{hash}:{index}/stopMix";
  public static final String REST_TX0 = REST_PREFIX + "tx0";
  public static final String REST_TX0_PREVIEW = REST_PREFIX + "tx0/preview";

  public static final String[] REST_ENDPOINTS =
      new String[] {
        REST_CLI,
        REST_CLI_INIT,
        REST_CLI_LOGIN,
        REST_CLI_LOGOUT,
        REST_CLI_RESTART,
        REST_CLI_RESYNC,
        REST_CLI_CONFIG,
        REST_POOLS,
        REST_WALLET_DEPOSIT,
        REST_MIX,
        REST_MIX_HISTORY,
        REST_MIX_HISTORY_EXTERNAL_XPUB,
        REST_MIX_START,
        REST_MIX_STOP,
        REST_UTXOS,
        REST_UTXO_STARTMIX,
        REST_UTXO_STOPMIX,
        REST_TX0_PREVIEW,
        REST_TX0
      };
}

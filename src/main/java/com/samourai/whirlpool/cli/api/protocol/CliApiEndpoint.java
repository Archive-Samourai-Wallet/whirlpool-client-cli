package com.samourai.whirlpool.cli.api.protocol;

public class CliApiEndpoint {
  public static final String REST_PREFIX = "/rest/";

  public static final String REST_CLI = REST_PREFIX + "cli";
  public static final String REST_CLI_INIT = REST_PREFIX + "cli/init";
  public static final String REST_CLI_CREATE = REST_PREFIX + "cli/create";
  public static final String REST_CLI_RESTORE_EXTERNAL = REST_PREFIX + "cli/restore/external";
  public static final String REST_CLI_RESTORE_BACKUP = REST_PREFIX + "cli/restore/backup";
  public static final String REST_CLI_OPEN_WALLET = REST_PREFIX + "cli/openWallet";
  public static final String REST_CLI_CLOSE_WALLET = REST_PREFIX + "cli/closeWallet";
  public static final String REST_CLI_RESTART = REST_PREFIX + "cli/restart";
  public static final String REST_CLI_RESYNC = REST_PREFIX + "cli/resync";
  public static final String REST_CLI_CONFIG = REST_PREFIX + "cli/config";

  public static final String REST_POOLS = REST_PREFIX + "pools";

  public static final String REST_WALLET_DEPOSIT = REST_PREFIX + "wallet/deposit";
  public static final String REST_WALLET_RECEIVE = REST_PREFIX + "wallet/receive";
  public static final String REST_WALLET_SPEND = REST_PREFIX + "wallet/spend";
  public static final String REST_WALLET_SPEND_PREVIEW = REST_PREFIX + "wallet/spend/preview";

  public static final String REST_MIX = REST_PREFIX + "mix";
  public static final String REST_MIX_START = REST_PREFIX + "mix/start";
  public static final String REST_MIX_STOP = REST_PREFIX + "mix/stop";

  public static final String REST_UTXOS = REST_PREFIX + "utxos";
  public static final String REST_UTXO_STARTMIX = REST_PREFIX + "utxos/{hash}:{index}/startMix";
  public static final String REST_UTXO_STOPMIX = REST_PREFIX + "utxos/{hash}:{index}/stopMix";
  public static final String REST_TX0 = REST_PREFIX + "tx0";
  public static final String REST_TX0_PREVIEWS = REST_PREFIX + "tx0/previews";

  public static final String REST_ADDRESS_SIGN = REST_PREFIX + "address/sign";
  public static final String REST_ADDRESS_PRIVATE = REST_PREFIX + "address/private";

  public static final String REST_SWEEP_PREVIEW = REST_PREFIX + "sweep/preview";
  public static final String REST_SWEEP_SUBMIT = REST_PREFIX + "sweep/submit";

  public static final String REST_TX_BOLTZMANN = REST_PREFIX + "tx/boltzmann";

  public static final String[] REST_ENDPOINTS =
      new String[] {
        REST_CLI,
        REST_CLI_INIT,
        REST_CLI_CREATE,
        REST_CLI_RESTORE_BACKUP,
        REST_CLI_RESTORE_EXTERNAL,
        REST_CLI_OPEN_WALLET,
        REST_CLI_CLOSE_WALLET,
        REST_CLI_RESTART,
        REST_CLI_RESYNC,
        REST_CLI_CONFIG,
        REST_POOLS,
        REST_WALLET_DEPOSIT,
        REST_WALLET_RECEIVE,
        REST_WALLET_SPEND,
        REST_WALLET_SPEND_PREVIEW,
        REST_MIX,
        REST_MIX_START,
        REST_MIX_STOP,
        REST_UTXOS,
        REST_UTXO_STARTMIX,
        REST_UTXO_STOPMIX,
        REST_TX0_PREVIEWS,
        REST_TX0,
        REST_ADDRESS_SIGN,
        REST_ADDRESS_PRIVATE,
        REST_SWEEP_PREVIEW,
        REST_SWEEP_SUBMIT,
        REST_TX_BOLTZMANN
      };

  public static final String WS_CONNECT = "connect";
  public static final String WS_WALLET = "wallet";
  public static final String WS_STATE = "state";
  public static final String WS_POOLS = "pools";
  public static final String WS_MIX_STATE = "mixState";
  public static final String WS_CHAIN = "chain";
  public static final String WS_MINER_FEE = "minerFee";
  public static final String WS_PAYNYM = "nym";

  public static final String WS_WALLET_REFRESH = "wallet/refresh";

  public static final String WS_PAYNYM_REFRESH = "nym/refresh";
  public static final String WS_PAYNYM_FOLLOW = "nym/follow";
  public static final String WS_PAYNYM_UNFOLLOW = "nym/unfollow";
  public static final String WS_PAYNYM_CLAIM = "nym/claim";

  public static final String WS_PREFIX = "/ws/";
  public static final String WS_PREFIX_DESTINATION = "/topic/";

  public static final String[] WS_ENDPOINTS =
      new String[] {
        WS_PREFIX + WS_CONNECT,
        WS_PREFIX + WS_WALLET_REFRESH,
        WS_PREFIX + WS_PAYNYM_REFRESH,
        WS_PREFIX + WS_PAYNYM_FOLLOW,
        WS_PREFIX + WS_PAYNYM_UNFOLLOW,
        WS_PREFIX + WS_PAYNYM_CLAIM,
      };

  public static final String WS_PREFIX_USER_PRIVATE = "/private";
  public static final String WS_PREFIX_USER_REPLY = "/reply";
}

package com.samourai.whirlpool.cli.services;

import com.google.common.eventbus.Subscribe;
import com.samourai.wallet.api.backend.MinerFee;
import com.samourai.wallet.api.backend.MinerFeeTarget;
import com.samourai.wallet.api.backend.beans.WalletResponse;
import com.samourai.wallet.api.paynym.beans.PaynymState;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.rest.*;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.event.CliStateChangeEvent;
import com.samourai.whirlpool.client.event.*;
import com.samourai.whirlpool.client.wallet.WhirlpoolEventService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.data.minerFee.MinerFeeSupplier;
import com.samourai.whirlpool.client.wallet.data.paynym.PaynymSupplier;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WsNotifierService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private WSMessageService wsMessageService;
  private CliWalletService cliWalletService;
  private CliConfig cliConfig;

  public WsNotifierService(
      WSMessageService wsMessageService, CliWalletService cliWalletService, CliConfig cliConfig) {
    this.wsMessageService = wsMessageService;
    this.cliWalletService = cliWalletService;
    this.cliConfig = cliConfig;
    WhirlpoolEventService.getInstance().register(this);
  }

  // WALLET DATA

  public ApiWalletDataResponse walletData() throws Exception {
    if (!cliWalletService.hasSessionWallet()) return null;
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    return new ApiWalletDataResponse(whirlpoolWallet);
  }

  @Subscribe
  public void onUtxoChanges(UtxoChangesEvent utxosChangeEvent) throws Exception {
    ApiWalletDataResponse walletData = walletData();
    if (walletData == null) {
      return;
    }
    wsMessageService.send(
        CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_WALLET, walletData);
  }

  // STATE

  public ApiCliStateResponse state() {
    return new ApiCliStateResponse(cliWalletService.getCliState(), cliConfig);
  }

  private void onStateChange() throws Exception {
    wsMessageService.send(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_STATE, state());
  }

  @Subscribe
  public void onWalletStart(WalletStartEvent walletStartEvent) throws Exception {
    onStateChange();

    // push all datas
    onChainBlockChange(null);
  }

  @Subscribe
  public void onWalletStop(WalletStopEvent walletStopEvent) throws Exception {
    onStateChange();
  }

  @Subscribe
  public void onWalletOpen(WalletOpenEvent walletOpenEvent) throws Exception {
    onStateChange();
  }

  @Subscribe
  public void onWalletClose(WalletCloseEvent walletCloseEvent) throws Exception {
    onStateChange();
  }

  @Subscribe
  public void onCliStateChange(CliStateChangeEvent cliStateChangeEvent) throws Exception {
    onStateChange();
  }

  // POOLS

  public ApiPoolsResponse pools() throws Exception {
    if (!cliWalletService.hasSessionWallet()) return null;
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    return new ApiPoolsResponse(whirlpoolWallet);
  }

  @Subscribe
  public void onPoolsChange(PoolsChangeEvent poolsChangeEvent) throws Exception {
    ApiPoolsResponse pools = pools();
    if (pools == null) {
      return;
    }
    wsMessageService.send(CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_POOLS, pools);
  }

  // MIX STATE

  public ApiMixStateResponse mixState() throws Exception {
    if (!cliWalletService.hasSessionWallet()) return null;
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    return new ApiMixStateResponse(whirlpoolWallet.getMixingState());
  }

  @Subscribe
  public void onMixStateChange(MixStateChangeEvent mixStateChangeEvent) throws Exception {
    ApiMixStateResponse mixState = mixState();
    if (mixState == null) {
      return;
    }
    wsMessageService.send(
        CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_MIX_STATE, mixState);
  }

  // CHAIN

  public ApiChainDataResponse chainData(WalletResponse.InfoBlock block) throws Exception {
    if (block == null) {
      if (!cliWalletService.hasSessionWallet()) return null;
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      block = whirlpoolWallet.getChainSupplier().getLatestBlock();
    }
    return new ApiChainDataResponse(block);
  }

  @Subscribe
  public void onChainBlockChange(ChainBlockChangeEvent chainBlockChangeEvent) throws Exception {
    ApiChainDataResponse chainData =
        chainData(chainBlockChangeEvent != null ? chainBlockChangeEvent.getBlock() : null);
    if (chainData == null) {
      return;
    }
    wsMessageService.send(
        CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_CHAIN, chainData);
  }

  // MINER FEE

  public ApiMinerFeeResponse minerFee(MinerFee minerFee) throws Exception {
    if (minerFee == null) {
      if (!cliWalletService.hasSessionWallet()) return null;
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      MinerFeeSupplier minerFeeSupplier = whirlpoolWallet.getMinerFeeSupplier();

      // build fee map
      Map<String, Integer> feesMap = new LinkedHashMap<>();
      for (MinerFeeTarget feeTarget : MinerFeeTarget.values()) {
        String key = feeTarget.getValue();
        int value = minerFeeSupplier.getFee(feeTarget);
        feesMap.put(key, value);
      }
      minerFee = new MinerFee(feesMap);
    }
    return new ApiMinerFeeResponse(minerFee);
  }

  @Subscribe
  public void onMinerFeeChange(MinerFeeChangeEvent minerFeeChangeEvent) throws Exception {
    ApiMinerFeeResponse minerFee =
        minerFee(minerFeeChangeEvent != null ? minerFeeChangeEvent.getMinerFee() : null);
    if (minerFee == null) {
      return;
    }
    wsMessageService.send(
        CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_MINER_FEE, minerFee);
  }

  // PAYNYM

  public ApiPaynymResponse paynym(PaynymState paynymState) throws Exception {
    if (paynymState == null) {
      if (!cliWalletService.hasSessionWallet()) return null;
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      PaynymSupplier paynymSupplier = whirlpoolWallet.getPaynymSupplier();
      paynymState = paynymSupplier.getPaynymState();
    }
    return new ApiPaynymResponse(paynymState);
  }

  @Subscribe
  public void onPaynymChange(PaynymChangeEvent paynymChangeEvent) throws Exception {
    ApiPaynymResponse response =
        paynym(paynymChangeEvent != null ? paynymChangeEvent.getPaynymState() : null);
    if (response == null) {
      return;
    }
    wsMessageService.send(
        CliApiEndpoint.WS_PREFIX_DESTINATION + CliApiEndpoint.WS_PAYNYM, response);
  }
}

package com.samourai.rpc.client;

import java.util.Optional;
import org.bitcoinj.core.Transaction;

public interface RpcClientService {
  boolean testConnectivity();

  Optional<RpcRawTransactionResponse> getRawTransaction(String txid);

  void broadcastTransaction(Transaction tx) throws Exception;
}

package com.samourai.whirlpool.cli.api.protocol.beans;

import com.samourai.wallet.api.backend.beans.WalletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ApiTx {
  private String hash;
  private long time;
  private long result;
  private Integer blockHeight; // null when unconfirmed
  private long balance;
  private Collection<ApiTxInput> inputs;
  private Collection<ApiTxOutput> outputs;
  private String address;

  public ApiTx(WalletResponse.Tx tx) {
    this.hash = tx.hash;
    this.time = tx.time;
    this.result = tx.result;
    this.blockHeight = tx.block_height;
    this.balance = tx.balance;

    this.inputs =
        Arrays.stream(tx.inputs)
            .map(
                new Function<WalletResponse.TxInput, ApiTxInput>() {
                  @Override
                  public ApiTxInput apply(WalletResponse.TxInput txInput) {
                    return new ApiTxInput(txInput);
                  }
                })
            .collect(Collectors.toList());

    this.outputs =
        Arrays.stream(tx.out)
            .map(
                new Function<WalletResponse.TxOutput, ApiTxOutput>() {
                  @Override
                  public ApiTxOutput apply(WalletResponse.TxOutput txOutput) {
                    return new ApiTxOutput(txOutput);
                  }
                })
            .collect(Collectors.toList());

    // find address
    this.address = findAddress();
  }

  private String findAddress() {
    Iterator<ApiTxOutput> itOuts = this.outputs.iterator();
    while (itOuts.hasNext()) {
      ApiTxOutput out = itOuts.next();
      if (!StringUtils.isEmpty(out.getXpub()) && !StringUtils.isEmpty(out.getAddr())) {
        return out.getAddr();
      }
    }
    Iterator<ApiTxInput> itIns = this.inputs.iterator();
    while (itIns.hasNext()) {
      ApiTxInput input = itIns.next();
      if (input.getPrevOut() != null
          && !StringUtils.isEmpty(input.getPrevOut().getXpub())
          && !StringUtils.isEmpty(input.getPrevOut().getAddr())) {
        return input.getPrevOut().getAddr();
      }
    }
    return null;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getResult() {
    return result;
  }

  public void setResult(long result) {
    this.result = result;
  }

  public Integer getBlockHeight() {
    return blockHeight;
  }

  public void setBlockHeight(Integer blockHeight) {
    this.blockHeight = blockHeight;
  }

  public long getBalance() {
    return balance;
  }

  public void setBalance(long balance) {
    this.balance = balance;
  }

  public Collection<ApiTxInput> getInputs() {
    return inputs;
  }

  public void setInputs(Collection<ApiTxInput> inputs) {
    this.inputs = inputs;
  }

  public Collection<ApiTxOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(Collection<ApiTxOutput> outputs) {
    this.outputs = outputs;
  }

  public String getAddress() {
    return address;
  }
}

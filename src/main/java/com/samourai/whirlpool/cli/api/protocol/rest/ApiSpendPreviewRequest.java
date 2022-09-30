package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.backend.MinerFeeTarget;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiUtxoRef;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import java.util.List;
import javax.validation.constraints.NotEmpty;

public class ApiSpendPreviewRequest {
  public WhirlpoolAccount account;
  public List<ApiUtxoRef> spendFrom;
  @NotEmpty public String spendTo;
  public long spendAmount;
  public MinerFeeTarget minerFeeTarget; // nullable
  public Integer minerFeeSatPerByte; // nullable
  public boolean stonewall;
  public boolean rbfOptIn;
  public ApiSpendRicochet ricochet; // nullable
  public ApiSpendCahoots cahoots; // nullable

  public ApiSpendPreviewRequest() {}

  public static class ApiSpendRicochet {
    public int hops;
    public boolean useTimeLock;
  }

  public static class ApiSpendCahoots {
    public String cahootsType; // TODO cannot use enum due to @JsonValue on CahootsType.value
    public String paymentCodeCounterparty;
  }
}

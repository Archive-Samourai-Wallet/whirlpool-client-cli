package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.paynym.beans.PaynymClaim;

public class ApiPaynymClaimResponse {
  private String paymentCode;
  private String nymID;
  private String nymName;
  private String nymAvatar;

  public ApiPaynymClaimResponse(PaynymClaim paynymClaim) {
    this.paymentCode = paynymClaim.getPaymentCode();
    this.nymID = paynymClaim.getNymID();
    this.nymName = paynymClaim.getNymName();
    this.nymAvatar = paynymClaim.getNymAvatar();
  }

  public String getPaymentCode() {
    return paymentCode;
  }

  public String getNymID() {
    return nymID;
  }

  public String getNymName() {
    return nymName;
  }

  public String getNymAvatar() {
    return nymAvatar;
  }
}

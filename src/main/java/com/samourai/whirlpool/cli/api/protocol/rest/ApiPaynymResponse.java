package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.api.paynym.beans.PaynymContact;
import com.samourai.wallet.api.paynym.beans.PaynymState;
import java.util.Collection;

public class ApiPaynymResponse {
  private boolean claimed;
  private String paymentCode;
  private String nymID;
  private String nymName;
  private String nymAvatar;
  private Boolean segwit;
  private Collection<PaynymContact> following;
  private Collection<PaynymContact> followers;

  public ApiPaynymResponse(PaynymState paynymState) {
    this.claimed = paynymState.isClaimed();
    this.paymentCode = paynymState.getPaymentCode();
    this.nymID = paynymState.getNymID();
    this.nymName = paynymState.getNymName();
    this.nymAvatar = paynymState.getNymAvatar();
    this.segwit = paynymState.isSegwit();
    this.following = paynymState.getFollowing();
    this.followers = paynymState.getFollowers();
  }

  public boolean isClaimed() {
    return claimed;
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

  public Boolean isSegwit() {
    return segwit;
  }

  public Collection<PaynymContact> getFollowing() {
    return following;
  }

  public Collection<PaynymContact> getFollowers() {
    return followers;
  }
}

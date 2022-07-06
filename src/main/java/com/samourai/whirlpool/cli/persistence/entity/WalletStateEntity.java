package com.samourai.whirlpool.cli.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "wallet_state")
public class WalletStateEntity {

  @Id
  @Column(nullable = false)
  public String id;

  @Column(nullable = false)
  public int valueInt;

  public WalletStateEntity() {}

  public WalletStateEntity(String id, int valueInt) {
    this.id = id;
    this.valueInt = valueInt;
  }
}

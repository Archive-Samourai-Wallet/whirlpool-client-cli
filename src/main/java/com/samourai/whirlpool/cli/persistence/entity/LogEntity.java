package com.samourai.whirlpool.cli.persistence.entity;

import com.samourai.whirlpool.cli.persistence.beans.LogType;
import com.samourai.whirlpool.client.mix.handler.DestinationType;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import javax.persistence.*;

@Entity
@Table(name = "log")
public class LogEntity {

  @Id
  @Column(nullable = false)
  @GeneratedValue
  public long id;

  @Column(nullable = false)
  public long created;

  @Column(nullable = false)
  public LogType logType;

  @Column(nullable = true, columnDefinition = "text")
  public String data;

  @Column(nullable = false)
  public Long amount;

  @Column(nullable = false)
  public WhirlpoolAccount fromAccount;

  @Column(nullable = false)
  public String fromHash;

  @Column(nullable = false)
  public Integer fromIndex;

  @Column(nullable = false)
  public String fromAddress;

  @Column(nullable = true)
  public DestinationType destinationType;

  @Column(nullable = true)
  public String toHash;

  @Column(nullable = true)
  public Integer toIndex;

  @Column(nullable = true)
  public String toAddress;

  public LogEntity() {}

  public LogEntity(
      long created,
      LogType logType,
      String data,
      Long amount,
      WhirlpoolAccount fromAccount,
      String fromHash,
      Integer fromIndex,
      String fromAddress,
      DestinationType destinationType,
      String toHash,
      Integer toIndex,
      String toAddress) {
    this.created = created;
    this.logType = logType;
    this.data = data;
    this.amount = amount;
    this.fromAccount = fromAccount;
    this.fromHash = fromHash;
    this.fromIndex = fromIndex;
    this.fromAddress = fromAddress;
    this.destinationType = destinationType;
    this.toHash = toHash;
    this.toIndex = toIndex;
    this.toAddress = toAddress;
  }
}

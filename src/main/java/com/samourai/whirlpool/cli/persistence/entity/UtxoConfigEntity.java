package com.samourai.whirlpool.cli.persistence.entity;

import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfigPersisted;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "utxo_config")
public class UtxoConfigEntity {

  @Id
  @Column(nullable = false)
  public String id;

  @Column(nullable = false)
  public int mixsDone;

  @Column(nullable = false)
  public boolean blocked;

  @Column(nullable = true)
  public String note;

  public UtxoConfigEntity() {}

  public UtxoConfigEntity(String id, UtxoConfigPersisted utxoConfigPersisted) {
    this.id = id;
    this.mixsDone = utxoConfigPersisted.getMixsDone();
    this.blocked = utxoConfigPersisted.isBlocked();
    this.note = utxoConfigPersisted.getNote();
  }

  public UtxoConfigPersisted toUtxoConfigPersisted() {
    return new UtxoConfigPersisted(mixsDone, null, blocked, note);
  }
}

package com.samourai.whirlpool.cli.persistence.entity;

import com.samourai.boltzmann.beans.BoltzmannResult;
import com.samourai.whirlpool.cli.persistence.beans.BoltzmannData;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.lang.invoke.MethodHandles;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "boltzmann")
public class BoltzmannEntity {

  @Id
  @Column(nullable = false)
  public String txid;

  @Column(nullable = false)
  public int nbCmbn;

  @Column(nullable = false)
  public Double entropy;

  @Column(nullable = false)
  public Double efficiency;

  @Column(nullable = false, columnDefinition = "text")
  @Convert(converter = BoltzmannDataConverter.class)
  public BoltzmannData data;

  public BoltzmannEntity() {}

  public BoltzmannEntity(String txid, BoltzmannResult boltzmannResult) {
    this.txid = txid;
    this.nbCmbn = boltzmannResult.getNbCmbn();
    this.entropy = boltzmannResult.getEntropy();
    this.efficiency = boltzmannResult.getEfficiency();
    this.data = new BoltzmannData(boltzmannResult);
  }

  @Converter
  public static class BoltzmannDataConverter implements AttributeConverter<BoltzmannData, String> {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public String convertToDatabaseColumn(BoltzmannData o) {
      return ClientUtils.toJsonString(o);
    }

    @Override
    public BoltzmannData convertToEntityAttribute(String s) {
      try {
        return ClientUtils.fromJson(s, BoltzmannData.class);
      } catch (Exception e) {
        log.error("", e);
        return null;
      }
    }
  }
}

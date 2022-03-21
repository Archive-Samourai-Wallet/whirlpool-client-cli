package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.whirlpool.cli.persistence.beans.BoltzmannData;
import com.samourai.whirlpool.cli.persistence.entity.Boltzmann;
import java.util.Map;

public class ApiTxBoltzmannResponse {
  private int nbCmbn;
  private int[][] matLnkCombinations;
  private double[][] matLnkProbabilities;

  private Map<String, Long> inputs;
  private Map<String, Long> outputs;

  private Double entropy;
  private long fees;

  private long feesMaker;
  private long feesTaker;
  private boolean hasFees;

  private Double efficiency;
  private Double nbCmbnPrfctCj;

  private int prfctCjNbInputs;
  private int prfctCjNbOutputs;

  private String[][] dtrmLnks;
  private long duration;

  public ApiTxBoltzmannResponse(Boltzmann boltzmann) {
    BoltzmannData boltzmannData = boltzmann.data;
    this.nbCmbn = boltzmannData.getNbCmbn();
    this.matLnkCombinations = boltzmannData.getMatLnkCombinations();

    this.inputs = boltzmannData.getInputs();
    this.outputs = boltzmannData.getOutputs();

    this.matLnkProbabilities = boltzmannData.getMatLnkProbabilities();
    this.entropy = boltzmannData.getEntropy();
    this.fees = boltzmannData.getFees();

    this.feesMaker = boltzmannData.getFeesMaker();
    this.feesTaker = boltzmannData.getFeesTaker();
    this.hasFees = boltzmannData.isHasFees();

    this.efficiency = boltzmannData.getEfficiency();
    this.nbCmbnPrfctCj = boltzmannData.getNbCmbnPrfctCj();

    this.prfctCjNbInputs = boltzmannData.getPrfctCjNbInputs();
    this.prfctCjNbOutputs = boltzmannData.getPrfctCjNbOutputs();

    this.dtrmLnks = boltzmannData.getDtrmLnks();
    this.duration = boltzmannData.getDuration();
  }

  public int getNbCmbn() {
    return nbCmbn;
  }

  public int[][] getMatLnkCombinations() {
    return matLnkCombinations;
  }

  public Map<String, Long> getInputs() {
    return inputs;
  }

  public Map<String, Long> getOutputs() {
    return outputs;
  }

  public double[][] getMatLnkProbabilities() {
    return matLnkProbabilities;
  }

  public Double getEntropy() {
    return entropy;
  }

  public long getFees() {
    return fees;
  }

  public long getFeesMaker() {
    return feesMaker;
  }

  public long getFeesTaker() {
    return feesTaker;
  }

  public boolean isHasFees() {
    return hasFees;
  }

  public Double getEfficiency() {
    return efficiency;
  }

  public Double getNbCmbnPrfctCj() {
    return nbCmbnPrfctCj;
  }

  public int getPrfctCjNbInputs() {
    return prfctCjNbInputs;
  }

  public int getPrfctCjNbOutputs() {
    return prfctCjNbOutputs;
  }

  public String[][] getDtrmLnks() {
    return dtrmLnks;
  }

  public long getDuration() {
    return duration;
  }
}

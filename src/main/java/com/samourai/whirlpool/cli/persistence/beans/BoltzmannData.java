package com.samourai.whirlpool.cli.persistence.beans;

import com.samourai.boltzmann.beans.BoltzmannResult;
import it.unimi.dsi.fastutil.doubles.DoubleBigList;
import it.unimi.dsi.fastutil.ints.IntBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import java.util.Map;

public class BoltzmannData {
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

  public BoltzmannData() {}

  public BoltzmannData(BoltzmannResult boltzmannResult) {
    this.nbCmbn = boltzmannResult.getNbCmbn();
    this.matLnkCombinations = toIntArray(boltzmannResult.getMatLnkCombinations());

    this.inputs = boltzmannResult.getTxos().getInputs();
    this.outputs = boltzmannResult.getTxos().getOutputs();

    this.matLnkProbabilities = toDoubleArray(boltzmannResult.getMatLnkProbabilities());
    this.entropy = boltzmannResult.getEntropy();
    this.fees = boltzmannResult.getFees();

    this.feesMaker = boltzmannResult.getIntraFees().getFeesMaker();
    this.feesTaker = boltzmannResult.getIntraFees().getFeesTaker();
    this.hasFees = boltzmannResult.getIntraFees().hasFees();

    this.efficiency = boltzmannResult.getEfficiency();
    this.nbCmbnPrfctCj = boltzmannResult.getNbCmbnPrfctCj();

    this.prfctCjNbInputs = boltzmannResult.getNbTxosPrfctCj().getNbIns();
    this.prfctCjNbOutputs = boltzmannResult.getNbTxosPrfctCj().getNbOuts();

    this.dtrmLnks = boltzmannResult.getDtrmLnks();
    this.duration = boltzmannResult.getDuration();
  }

  private int[][] toIntArray(ObjectBigList<IntBigList> v) {
    if (v == null) return null;
    int[][] result = new int[(int) v.size64()][];
    int i = 0;
    for (IntBigList vv : v) {
      result[i++] = vv.toIntArray();
    }
    return result;
  }

  private double[][] toDoubleArray(ObjectBigList<DoubleBigList> v) {
    if (v == null) return null;
    double[][] result = new double[(int) v.size64()][];
    int i = 0;
    for (DoubleBigList vv : v) {
      result[i++] = vv.toDoubleArray();
    }
    return result;
  }

  public int getNbCmbn() {
    return nbCmbn;
  }

  public void setNbCmbn(int nbCmbn) {
    this.nbCmbn = nbCmbn;
  }

  public int[][] getMatLnkCombinations() {
    return matLnkCombinations;
  }

  public void setMatLnkCombinations(int[][] matLnkCombinations) {
    this.matLnkCombinations = matLnkCombinations;
  }

  public double[][] getMatLnkProbabilities() {
    return matLnkProbabilities;
  }

  public void setMatLnkProbabilities(double[][] matLnkProbabilities) {
    this.matLnkProbabilities = matLnkProbabilities;
  }

  public Map<String, Long> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, Long> inputs) {
    this.inputs = inputs;
  }

  public Map<String, Long> getOutputs() {
    return outputs;
  }

  public void setOutputs(Map<String, Long> outputs) {
    this.outputs = outputs;
  }

  public Double getEntropy() {
    return entropy;
  }

  public void setEntropy(Double entropy) {
    this.entropy = entropy;
  }

  public long getFees() {
    return fees;
  }

  public void setFees(long fees) {
    this.fees = fees;
  }

  public long getFeesMaker() {
    return feesMaker;
  }

  public void setFeesMaker(long feesMaker) {
    this.feesMaker = feesMaker;
  }

  public long getFeesTaker() {
    return feesTaker;
  }

  public void setFeesTaker(long feesTaker) {
    this.feesTaker = feesTaker;
  }

  public boolean isHasFees() {
    return hasFees;
  }

  public void setHasFees(boolean hasFees) {
    this.hasFees = hasFees;
  }

  public Double getEfficiency() {
    return efficiency;
  }

  public void setEfficiency(Double efficiency) {
    this.efficiency = efficiency;
  }

  public Double getNbCmbnPrfctCj() {
    return nbCmbnPrfctCj;
  }

  public void setNbCmbnPrfctCj(Double nbCmbnPrfctCj) {
    this.nbCmbnPrfctCj = nbCmbnPrfctCj;
  }

  public int getPrfctCjNbInputs() {
    return prfctCjNbInputs;
  }

  public void setPrfctCjNbInputs(int prfctCjNbInputs) {
    this.prfctCjNbInputs = prfctCjNbInputs;
  }

  public int getPrfctCjNbOutputs() {
    return prfctCjNbOutputs;
  }

  public void setPrfctCjNbOutputs(int prfctCjNbOutputs) {
    this.prfctCjNbOutputs = prfctCjNbOutputs;
  }

  public String[][] getDtrmLnks() {
    return dtrmLnks;
  }

  public void setDtrmLnks(String[][] dtrmLnks) {
    this.dtrmLnks = dtrmLnks;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }
}

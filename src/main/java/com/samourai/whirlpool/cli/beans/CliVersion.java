package com.samourai.whirlpool.cli.beans;

public enum CliVersion {
  VERSION_4(4),
  VERSION_5(5);

  private int version;

  private CliVersion(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }
}

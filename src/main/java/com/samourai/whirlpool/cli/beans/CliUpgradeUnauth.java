package com.samourai.whirlpool.cli.beans;

public abstract class CliUpgradeUnauth extends CliUpgrade {

  public CliUpgradeUnauth() {}

  public abstract boolean run() throws Exception;
}

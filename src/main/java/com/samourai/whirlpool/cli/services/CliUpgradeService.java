package com.samourai.whirlpool.cli.services;

import com.samourai.whirlpool.cli.beans.CliUpgrade;
import com.samourai.whirlpool.cli.beans.CliUpgradeAuth;
import com.samourai.whirlpool.cli.beans.CliUpgradeUnauth;
import com.samourai.whirlpool.cli.beans.CliVersion;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.wallet.CliWallet;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CliUpgradeService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final CliVersion CURRENT_VERSION = CliVersion.VERSION_6;

  private CliConfig cliConfig;
  private CliConfigService cliConfigService;
  private LinkedHashMap<Integer, CliUpgrade> upgrades;

  public CliUpgradeService(CliConfig cliConfig, CliConfigService cliConfigService) {
    this.cliConfig = cliConfig;
    this.cliConfigService = cliConfigService;
    this.upgrades = new LinkedHashMap<>();

    // V4
    this.upgrades.put(
        CliVersion.VERSION_4.getVersion(),
        new CliUpgradeUnauth() {
          @Override
          public boolean run() throws Exception {
            // set cli.mix.clients=5 when missing
            if (cliConfig.getMix().getClients() == 0) {
              Properties props = cliConfigService.loadProperties();
              props.put(CliConfigService.KEY_MIX_CLIENTS, "5");
              cliConfigService.saveProperties(props);
              return true; // restart
            }
            return false;
          }
        });

    // V5
    this.upgrades.put(
        CliVersion.VERSION_5.getVersion(),
        new CliUpgradeAuth() {
          @Override
          public boolean run(CliWallet cliWallet) throws Exception {
            // resync postmix counters
            try {
              cliWallet.resync();
            } catch (Exception e) {
              log.error("", e);
            }
            return false;
          }
        });

    // V6
    this.upgrades.put(
        CliVersion.VERSION_6.getVersion(),
        new CliUpgradeAuth() {
          @Override
          public boolean run(CliWallet cliWallet) throws Exception {
            // remove cli.mix.mixsTarget when present
            Properties props = cliConfigService.loadProperties();
            final String KEY_MIX_MIXSTARGET = "cli.mix.mixsTarget";
            if (props.containsKey(KEY_MIX_MIXSTARGET)) {
              props.remove(KEY_MIX_MIXSTARGET);
              cliConfigService.saveProperties(props);
              return true; // restart
            }
            return false;
          }
        });
  }

  public boolean upgradeUnauthenticated() throws Exception {
    // find next upgrade
    int localVersion = cliConfig.getVersion();
    int nextVersion = localVersion + 1;
    CliUpgradeUnauth cliUpgrade = (CliUpgradeUnauth) getNextUpgrade(nextVersion, false);
    if (cliUpgrade == null) {
      // up-to-date
      return false;
    }

    // run upgrade
    if (log.isDebugEnabled()) {
      log.debug(" • Upgrading CLI (unauth): " + localVersion + " -> " + nextVersion);
    }
    boolean shouldRestart = cliUpgrade.run();
    afterUpgrade(nextVersion, shouldRestart);
    return shouldRestart;
  }

  public boolean upgradeAuthenticated(CliWallet cliWallet) throws Exception {
    // find next upgrade
    int localVersion = cliConfig.getVersion();
    int nextVersion = localVersion + 1;
    CliUpgradeAuth cliUpgrade = (CliUpgradeAuth) getNextUpgrade(nextVersion, true);
    if (cliUpgrade == null) {
      // up-to-date
      return false;
    }

    // run upgrade
    if (log.isDebugEnabled()) {
      log.debug(" • Upgrading CLI (auth): " + localVersion + " -> " + nextVersion);
    }
    boolean shouldRestart = cliUpgrade.run(cliWallet);
    afterUpgrade(nextVersion, shouldRestart);
    return shouldRestart;
  }

  private CliUpgrade getNextUpgrade(int nextVersion, boolean authenticated) {
    if (nextVersion > CURRENT_VERSION.getVersion()) {
      // up-to-date
      return null;
    }

    // find next upgrade
    CliUpgrade cliUpgrade = upgrades.get(nextVersion);
    if (cliUpgrade == null) {
      // up-to-date
      return null;
    }

    if (authenticated) {
      if (!(cliUpgrade instanceof CliUpgradeAuth)) {
        // upgrade will run after authentication
        return null;
      }
    } else {
      if (!(cliUpgrade instanceof CliUpgradeUnauth)) {
        // should never happen
        return null;
      }
    }
    return cliUpgrade;
  }

  private void afterUpgrade(int nextVersion, boolean shouldRestart) throws Exception {
    cliConfigService.setVersion(nextVersion);
    if (shouldRestart) {
      cliConfigService.setCliStatusNotReady("Upgrade success. Restarting CLI...");
    }
  }
}

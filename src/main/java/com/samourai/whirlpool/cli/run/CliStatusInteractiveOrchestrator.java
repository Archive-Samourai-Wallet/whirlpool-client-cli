package com.samourai.whirlpool.cli.run;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.util.AbstractOrchestrator;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.exception.NoSessionWalletException;
import com.samourai.whirlpool.cli.services.CliWalletService;
import com.samourai.whirlpool.cli.utils.CliUtils;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliStatusInteractiveOrchestrator extends AbstractOrchestrator {
  private static final Logger log = LoggerFactory.getLogger(CliStatusInteractiveOrchestrator.class);

  private CliWalletService cliWalletService;
  private CliConfig cliConfig;

  public CliStatusInteractiveOrchestrator(
      int loopDelay, CliWalletService cliWalletService, CliConfig cliConfig) {
    super(loopDelay);
    this.cliWalletService = cliWalletService;
    this.cliConfig = cliConfig;
  }

  @Override
  protected void runOrchestrator() {
    interactive();
  }

  private void interactive() {
    while (isStarted()) {
      try {
        Character car = CliUtils.readChar();
        if (car != null) {
          car = Character.toUpperCase(car);
          if (car.equals('T')) {
            printThreads();
          } else if (car.equals('D')) {
            printUtxos(WhirlpoolAccount.DEPOSIT);
          } else if (car.equals('P')) {
            printUtxos(WhirlpoolAccount.PREMIX);
          } else if (car.equals('O')) {
            printUtxos(WhirlpoolAccount.POSTMIX);
          } else if (car.equals('S')) {
            printSystem();
          }
        } else {
          if (log.isDebugEnabled()) {
            log.debug("console input was null");
          }
        }
      } catch (Exception e) {
        log.error("", e);
      }
    }
  }

  private void printThreads() {
    try {
      WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
      MixingState mixingState = whirlpoolWallet.getMixingState();
      log.info("⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿");
      log.info("⣿ MIXING THREADS:");

      String lineFormat = "| %8s | %25s | %8s | %10s | %10s | %8s | %68s | %14s | %8s | %6s |\n";
      StringBuilder sb = new StringBuilder();
      sb.append(
          String.format(
              lineFormat,
              "THREAD",
              "STATUS",
              "SINCE",
              "ACCOUNT",
              "BALANCE",
              "CONFIRMS",
              "UTXO",
              "PATH",
              "POOL",
              "MIXS"));

      int i = 0;
      long now = System.currentTimeMillis();
      for (WhirlpoolUtxo whirlpoolUtxo : mixingState.getUtxosMixing()) {
        MixProgress mixProgress = whirlpoolUtxo.getUtxoState().getMixProgress();
        String progress = mixProgress != null ? mixProgress.toString() : "";
        String since = mixProgress != null ? ((now - mixProgress.getSince()) / 1000) + "s" : "";
        UnspentOutput o = whirlpoolUtxo.getUtxo();
        String utxo = o.tx_hash + ":" + o.tx_output_n;
        sb.append(
            String.format(
                lineFormat,
                "#" + (i + 1),
                progress,
                since,
                whirlpoolUtxo.getAccount().name(),
                ClientUtils.satToBtc(o.value),
                o.confirmations,
                utxo,
                o.getPath(),
                whirlpoolUtxo.getPoolId() != null ? whirlpoolUtxo.getPoolId() : "-",
                whirlpoolUtxo.getMixsDone()));

        i++;
      }
      log.info("\n" + sb.toString());
    } catch (NoSessionWalletException e) {
      System.out.print("⣿ Wallet CLOSED\r");
    } catch (Exception e) {
      log.error("", e);
    }
  }

  private void printSystem() {
    ThreadGroup tg = Thread.currentThread().getThreadGroup();
    Collection<Thread> threadSet =
        Thread.getAllStackTraces()
            .keySet()
            .stream()
            .filter(t -> t.getThreadGroup() == tg)
            .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
            .collect(Collectors.toList());
    log.info("⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿");
    log.info("⣿ SYSTEM THREADS:");
    int i = 0;
    for (Thread t : threadSet) {
      log.info("#" + i + " " + t + ":" + "" + t.getState());
      // show trace for BLOCKED
      if (Thread.State.BLOCKED.equals(t.getState())) {
        log.info(StringUtils.join(t.getStackTrace(), "\n"));
      }
      i++;
    }

    // memory
    Runtime rt = Runtime.getRuntime();
    long total = rt.totalMemory();
    long free = rt.freeMemory();
    long used = total - free;
    log.info("⣿ MEM USE: " + CliUtils.bytesToMB(used) + "M/" + CliUtils.bytesToMB(total) + "M");
  }

  private void printUtxos(WhirlpoolAccount account) throws Exception {
    WhirlpoolWallet whirlpoolWallet = cliWalletService.getSessionWallet();
    Collection<WhirlpoolUtxo> utxos = whirlpoolWallet.getUtxoSupplier().findUtxos(account);
    try {
      log.info("⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿");
      log.info("⣿ " + account.name() + " UTXOS:");
      ClientUtils.logWhirlpoolUtxos(utxos);

    } catch (Exception e) {
      log.error("", e);
    }
  }
}

package com.samourai.whirlpool.cli.utils;

import com.google.common.primitives.Bytes;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.utils.ClientUtils;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class WalletRoutingDataSource extends AbstractDataSource {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String PASSWORD_SEED = "_%!-WhirlpoolDb-!%_";

  private static final String PUBLIC_IDENTIFIER = "default";
  private static final String PUBLIC_PASSWORD = "_%!-WhirlpoolDbDefault-!%_";

  private DataSource dataSourceWallet;
  private DataSource dataSourcePublic;

  public WalletRoutingDataSource() throws NotifiableException {
    this.dataSourceWallet = null;
    this.dataSourcePublic = computeDataSource(PUBLIC_IDENTIFIER, PUBLIC_PASSWORD);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.determineTargetDataSource().getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return this.determineTargetDataSource().getConnection(username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return iface.isInstance(this) ? (T) this : this.determineTargetDataSource().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this) || this.determineTargetDataSource().isWrapperFor(iface);
  }

  protected DataSource determineTargetDataSource() {
    if (dataSourceWallet != null) {
      if (log.isTraceEnabled()) {
        log.trace("using dataSourceWallet");
      }
      return dataSourceWallet;
    }
    if (log.isTraceEnabled()) {
      log.trace("using dataSourcePublic");
    }
    return dataSourcePublic;
  }

  public void setDataSourceWallet(String walletIdentifier, String passphrase)
      throws NotifiableException {
    if (log.isDebugEnabled()) {
      log.debug("setDataSource: " + walletIdentifier);
    }
    this.dataSourceWallet = computeDataSource(walletIdentifier, passphrase);
  }

  public void clearDataSourceWallet() {
    this.dataSourceWallet = null;
  }

  private DataSource computeDataSource(String identifier, String password)
      throws NotifiableException {
    return computeDataSource(identifier, password, true);
  }

  private DataSource computeDataSource(String identifier, String password, boolean retry)
      throws NotifiableException {
    String pw = computePassword(password);
    String fileName = "./whirlpool-cli-db-" + identifier;
    DataSource dataSource =
        DataSourceBuilder.create()
            .username("sa")
            .password(pw + " " + pw)
            .url("jdbc:h2:file:" + fileName + ";CIPHER=AES")
            .driverClassName("org.h2.Driver")
            .build();
    try {
      if (log.isDebugEnabled()) {
        log.debug("Migrating db: " + fileName);
      }
      migrateDb(dataSource);
      if (log.isDebugEnabled()) {
        log.debug("Db migration success: " + fileName);
      }
    } catch (Exception e) {
      if (retry) {
        // db file corrupted => delete and retry
        File dbFile = new File(fileName + ".mv.db");
        if (dbFile.exists()) {
          // rename corrupted file to .old
          File dbFileOld = new File(fileName + ".mv.db.old");
          log.error(
              "Db seems corrupted, reseting: "
                  + dbFile.getAbsolutePath()
                  + " -> "
                  + dbFileOld.getAbsolutePath());
          dbFile.renameTo(dbFileOld);

          // retry
          return computeDataSource(identifier, password, false);
        } else {
          throw new NotifiableException("Unable to read database: " + dbFile.getAbsolutePath());
        }
      }
    }
    return dataSource;
  }

  private void migrateDb(DataSource dataSource) throws Exception {
    FluentConfiguration config = Flyway.configure();
    config.dataSource(dataSource);
    config.baselineOnMigrate(true); // applying to an existing database

    Flyway flyway = new Flyway(config);
    flyway.repair();
    flyway.migrate();
  }

  private String computePassword(String password) {
    return ClientUtils.sha256Hash(Bytes.concat(password.getBytes(), PASSWORD_SEED.getBytes()));
  }
}

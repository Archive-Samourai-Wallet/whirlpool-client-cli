package com.samourai.whirlpool.cli.utils;

import com.samourai.whirlpool.client.exception.NotifiableException;
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
  private static final String PASSWORD_SALT = "_%!-WhirlpoolDb-!%_";
  private static final String PASSWORD_MEMORY = "_%!-WhirlpoolDb-!%_";

  private DataSource dataSourceWallet;
  private DataSource dataSourceMemory;

  public WalletRoutingDataSource() throws NotifiableException {
    this.dataSourceWallet = null;
    this.dataSourceMemory = computeDataSourceMemory();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.getDataSource().getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return this.getDataSource().getConnection(username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return iface.isInstance(this) ? (T) this : this.getDataSource().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this) || this.getDataSource().isWrapperFor(iface);
  }

  public DataSource getDataSource() {
    if (dataSourceWallet != null) {
      return dataSourceWallet;
    }
    return dataSourceMemory;
  }

  public void setDataSourceWallet(String walletIdentifier, String passphrase)
      throws NotifiableException {
    if (log.isDebugEnabled()) {
      log.debug("setDataSource: " + walletIdentifier);
    }
    this.dataSourceWallet = computeDataSourceWallet(walletIdentifier, passphrase);
  }

  public void clearDataSourceWallet() {
    this.dataSourceWallet = null;
  }

  private DataSource computeDataSourceWallet(String identifier, String password)
      throws NotifiableException {
    String fileName = "./whirlpool-cli-db-" + identifier;
    String db = "file:" + fileName;
    try {
      return computeDataSource(db, password);
    } catch (Exception e) {
      // db file corrupted => delete and retry
      File dbFile = new File(fileName + ".mv.db");
      if (dbFile.exists()) {
        // rename corrupted file to .old
        File dbFileOld = new File(fileName + ".mv.db.old");
        log.error(
            "Db seems corrupted, resetting: "
                + dbFile.getAbsolutePath()
                + " -> "
                + dbFileOld.getAbsolutePath());
        dbFile.renameTo(dbFileOld);

        // retry
        try {
          return computeDataSource(db, password);
        } catch (Exception ee) {
          throw new NotifiableException(
              "Unable to initialize database: " + dbFile.getAbsolutePath());
        }
      } else {
        throw new NotifiableException("Unable to read database: " + dbFile.getAbsolutePath());
      }
    }
  }

  private DataSource computeDataSourceMemory() throws NotifiableException {
    String db = "mem:whirlpool-cli";
    try {
      return computeDataSource(db, PASSWORD_MEMORY);
    } catch (Exception e) {
      log.error("Unable to initialize database: mem", e);
      throw new NotifiableException("Unable to initialize database: mem", e);
    }
  }

  private DataSource computeDataSource(String db, String password) {
    String url = "jdbc:h2:" + db + ";CIPHER=AES";
    String pw = computePassword(password);
    if (log.isDebugEnabled()) {
      log.debug("computeDataSource: " + url);
    }
    DataSource dataSource =
        DataSourceBuilder.create()
            .username("sa")
            .password(pw + " " + pw)
            .url(url)
            .driverClassName("org.h2.Driver")
            .build();
    migrateDb(dataSource);
    return dataSource;
  }

  private void migrateDb(DataSource dataSource) {
    FluentConfiguration config = Flyway.configure();
    config.dataSource(dataSource);
    config.baselineOnMigrate(true); // applying to an existing database

    Flyway flyway = new Flyway(config);
    flyway.repair();
    flyway.migrate();
  }

  private String computePassword(String password) {
    return CliUtils.sha512Hash(password + PASSWORD_SALT);
  }
}

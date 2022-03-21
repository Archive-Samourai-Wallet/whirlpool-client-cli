package com.samourai.whirlpool.cli.services;

import com.samourai.whirlpool.client.test.AbstractTest;
import java.io.File;
import java.lang.invoke.MethodHandles;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DbServiceTest extends AbstractTest {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String EXPORT_FILE = "dump.sql";

  @Autowired private DataSource dataSource;

  public DbServiceTest() throws Exception {}

  @Test
  public void export() throws Exception {
    File dump = new File(EXPORT_FILE);
    if (dump.exists()) {
      dump.delete();
    }
    new JdbcTemplate(dataSource).execute("script to '" + dump.getAbsolutePath() + "'");
  }
}

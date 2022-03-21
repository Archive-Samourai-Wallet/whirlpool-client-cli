package com.samourai.whirlpool.cli.services;

import com.samourai.javawsserver.services.JWSSSessionService;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class WSSessionService extends JWSSSessionService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  public WSSessionService(TaskExecutor taskExecutor) {
    super(taskExecutor);
  }
}

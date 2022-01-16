package com.samourai.whirlpool.cli.api.controllers.rest.cli;

import com.samourai.whirlpool.cli.Application;
import com.samourai.whirlpool.cli.api.controllers.rest.AbstractRestController;
import com.samourai.whirlpool.cli.api.protocol.CliApiEndpoint;
import com.samourai.whirlpool.cli.api.protocol.beans.ApiCliConfig;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliConfigRequest;
import com.samourai.whirlpool.cli.api.protocol.rest.ApiCliConfigResponse;
import com.samourai.whirlpool.cli.config.CliConfig;
import com.samourai.whirlpool.cli.services.CliConfigService;
import com.samourai.whirlpool.cli.services.CliWalletService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
public class CliConfigController extends AbstractRestController {
  @Autowired private CliConfig cliConfig;
  @Autowired private CliConfigService cliConfigService;
  @Autowired private CliWalletService cliWalletService;

  @RequestMapping(value = CliApiEndpoint.REST_CLI_CONFIG, method = RequestMethod.GET)
  public ApiCliConfigResponse getCliConfig(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    ApiCliConfigResponse response = new ApiCliConfigResponse(cliConfig);
    return response;
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_CONFIG, method = RequestMethod.POST)
  public void setCliConfig(
      @RequestHeader HttpHeaders headers, @Valid @RequestBody ApiCliConfigRequest payload)
      throws Exception {
    checkHeaders(headers);

    // set config
    ApiCliConfig apiCliConfig = payload.getConfig();
    cliConfigService.setApiConfig(apiCliConfig);

    // restart
    Application.restart();
  }

  @RequestMapping(value = CliApiEndpoint.REST_CLI_CONFIG, method = RequestMethod.DELETE)
  public void resetCliConfig(@RequestHeader HttpHeaders headers) throws Exception {
    checkHeaders(headers);

    cliConfigService.resetConfiguration();
    Application.restart();
  }
}

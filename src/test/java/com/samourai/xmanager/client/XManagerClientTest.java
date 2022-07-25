package com.samourai.xmanager.client;

import com.samourai.http.client.HttpUsage;
import com.samourai.http.client.JavaHttpClient;
import com.samourai.whirlpool.client.test.AbstractTest;
import com.samourai.xmanager.protocol.XManagerService;
import com.samourai.xmanager.protocol.rest.AddressIndexResponse;
import io.reactivex.Observable;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XManagerClientTest extends AbstractTest {
  private static final boolean testnet = true;
  private static final long requestTimeout = 5000;

  private XManagerClient xManagerClient;
  private XManagerClient xManagerClientFailing;

  public XManagerClientTest() throws Exception {
    JavaHttpClient httpClient = new JavaHttpClient(requestTimeout, null, HttpUsage.BACKEND);
    xManagerClient = new XManagerClient(httpClient, testnet, false);

    JavaHttpClient httpClientFailing =
        new JavaHttpClient(requestTimeout, null, HttpUsage.BACKEND) {
          @Override
          public <T> Observable<Optional<T>> postJson(
              String urlStr, Class<T> responseType, Map<String, String> headers, Object bodyObj) {
            throw new RuntimeException("Failure");
          }
        };
    xManagerClientFailing = new XManagerClient(httpClientFailing, testnet, false);
  }

  @Test
  public void getAddressOrDefault() throws Exception {
    String address = xManagerClient.getAddressOrDefault(XManagerService.WHIRLPOOL);
    Assertions.assertNotNull(address);
    Assertions.assertNotEquals(XManagerService.WHIRLPOOL.getDefaultAddress(testnet), address);
  }

  @Test
  public void getAddressOrDefault_failure() throws Exception {
    String address = xManagerClientFailing.getAddressOrDefault(XManagerService.WHIRLPOOL);

    // silently fail and return default address
    Assertions.assertNotNull(address);
    Assertions.assertEquals(XManagerService.WHIRLPOOL.getDefaultAddress(testnet), address);
  }

  @Test
  public void getAddressIndexOrDefault() throws Exception {
    AddressIndexResponse addressIndexResponse =
        xManagerClient.getAddressIndexOrDefault(XManagerService.WHIRLPOOL);
    Assertions.assertNotNull(addressIndexResponse);
    Assertions.assertNotEquals(
        XManagerService.WHIRLPOOL.getDefaultAddress(testnet), addressIndexResponse.address);
    Assertions.assertTrue(addressIndexResponse.index > 0);
  }

  @Test
  public void getAddressIndexOrDefault_failure() throws Exception {
    AddressIndexResponse addressIndexResponse =
        xManagerClientFailing.getAddressIndexOrDefault(XManagerService.WHIRLPOOL);
    Assertions.assertEquals(
        XManagerService.WHIRLPOOL.getDefaultAddress(testnet), addressIndexResponse.address);
    Assertions.assertEquals(0, addressIndexResponse.index);
  }

  @Test
  public void verifyAddressIndexResponse() throws Exception {
    Assertions.assertTrue(
        xManagerClient.verifyAddressIndexResponse(
            XManagerService.WHIRLPOOL, "tb1q6m3urxjc8j2l8fltqj93jarmzn0975nnxuymnx", 0));
    Assertions.assertFalse(
        xManagerClient.verifyAddressIndexResponse(
            XManagerService.WHIRLPOOL, "tb1qz84ma37y3d759sdy7mvq3u4vsxlg2qahw3lm23", 0));

    Assertions.assertTrue(
        xManagerClient.verifyAddressIndexResponse(
            XManagerService.WHIRLPOOL, "tb1qcaerxclcmu9llc7ugh65hemqg6raaz4sul535f", 1));
    Assertions.assertFalse(
        xManagerClient.verifyAddressIndexResponse(
            XManagerService.WHIRLPOOL, "tb1qcfgn9nlgxu0ycj446prdkg0p36qy5a39pcf74v", 1));
  }

  @Test
  public void verifyAddressIndexResponse_failure() throws Exception {
    try {
      xManagerClientFailing.verifyAddressIndexResponse(
          XManagerService.WHIRLPOOL, "tb1qcfgn9nlgxu0ycj446prdkg0p36qy5a39pcf74v", 0);
      Assertions.assertTrue(false); // exception expected
    } catch (RuntimeException e) {
      // ok
    }
  }
}

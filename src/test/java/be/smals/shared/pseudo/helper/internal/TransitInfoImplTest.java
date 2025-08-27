package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import be.smals.shared.pseudo.helper.TransitInfo;
import org.junit.jupiter.api.Test;

public class TransitInfoImplTest {

  private final DomainImpl domain = createTestDomain("test", 8);

  @Test
  public void header() {
    final TransitInfo transitInfo = new TransitInfoImpl(domain, "eyJhdWQiOiJodHRwczovL2FwaS1hY3B0LmVoZWFsdGguZmdvdi5iZS9wc2V1ZG8vdjEvZG9tYWlucy9laGVhbHRoX3YxIiwiZW5jIjoiQTI1NkdDTSIsImV4cCI6MTcxODIwMzI4OCwiaWF0IjoxNzE4MjAyNjg4LCJhbGciOiJkaXIiLCJraWQiOiJiNTRjZTNlNC1lN2M1LTQ1NWYtODA4ZS02OWEwM2EzN2E4NWYifQ..zO-S0LyrwtQLb-x9.oB87loxuJfNmQbif4hHLh2Mvot17jxeqpBfsjayqyXVKMXB8-QMZYBY1OgwmWU7ZJKvbBU62f0I6FRZIoKMQjlPMoNNJmnc2FkaIpyi6TLAciZgdolJZwZgIN5_gdKdURIJBFOH_MEyZCCAcK6TuYRM98aGPV2SMU06RUnqrWZa1eie93w4u.SNkaDxhvpQaQL0aBumRLmQ");
    final var headers = transitInfo.header();
    assertEquals(6, headers.size());
    assertEquals("https://api-acpt.ehealth.fgov.be/pseudo/v1/domains/ehealth_v1", headers.get("aud"));
    assertEquals("A256GCM", headers.get("enc"));
    assertInstanceOf(Long.class, headers.get("exp"));
    assertEquals(Long.valueOf("1718203288"), headers.get("exp"));
    assertInstanceOf(Long.class, headers.get("iat"));
    assertEquals(Long.valueOf("1718202688"), headers.get("iat"));
    assertEquals("dir", headers.get("alg"));
    assertEquals("b54ce3e4-e7c5-455f-808e-69a03a37a85f", headers.get("kid"));
  }
}

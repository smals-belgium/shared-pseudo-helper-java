package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;

import be.smals.shared.pseudo.helper.Domain;
import org.junit.jupiter.api.Test;

public class PseudonymInTransitFactoryTest {

  private static final String sec1CompressedBase64 = "AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAx" +
                                                     "MjM0NTY3ODkxMAwAAAAAAAAAAQ";
  static final String sec1Base64 = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAx" +
                                   "MjM0NTY3ODkxMAwAAAAAAAAAAQAcQnkmJhoLpiMDrhcipitZfG4pU4y_jShJaXoI" +
                                   "vJnLJ90AeoKxuTx6YA_Fwkz0xoQ60-h7BrhJBDlcGdt5mkpYlQ";
  private static final String transitInfoRaw = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwia2lkIjoiMjAyMi0xMiIsImF1" +
                                               "ZCI6Imh0dHBzOi8vYXBpLWludC5laGVhbHRoLmZnb3YuYmUvcHNldWRvL3YxL2Rv" +
                                               "bWFpbnMvdWhtZXBfdjEifQ..osrl3KS4nkheJvcJ.pXN4Asfg8RGtsoV529YoFRW" +
                                               "P_XSXUViR-wxuvwYTvN9fMSDksq7qZMmmqDstyGyOidHKHrVvtqB0PFrek71P4K8" +
                                               "Rp0rDuvAc6RC2cbdwV08Ksw6t3Wf72H8c8QDKGKmYb84z_oH8TMnY26cAm0nC2Hb" +
                                               "18H-SXTh8xFXe3DK8y06wx4rAAXFZGsXayloJ6oweux_tvKQ4NSWi3gzhjVi0g-q" +
                                               "WR9TYZNj9NNyU9eeSDk9UsXJ8cugpvStu6oFOCbW4520fl6h5oaJ7Rye3.IEd7uL" +
                                               "w-ICHAwqIzfrjOFw";
  private static final String x = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMDEyMzQ1Njc4OTEwDAAAAAAAAAAB";
  private static final String y = "ABxCeSYmGgumIwOuFyKmK1l8bilTjL+NKElpegi8mcsn3QB6grG5PHpgD8XCTPTGhDrT6HsGuEkEOVwZ23maSliV";

  private static final Domain domain = createTestDomain("test", 8);

  @Test
  void fromXYAndTransitInfo_asString() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromXYAndTransitInfo(x, y, transitInfoRaw);
    assertEquals(transitInfoRaw, pseudonymInTransit.transitInfo().asString());
    assertEquals(sec1Base64, pseudonymInTransit.pseudonym().asString());
  }

  @Test
  void fromXYAndTransitInfo_asShortString() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromXYAndTransitInfo(x, y, transitInfoRaw);
    assertEquals(transitInfoRaw, pseudonymInTransit.transitInfo().asString());
    assertEquals(sec1CompressedBase64, pseudonymInTransit.pseudonym().asShortString());
  }

  @Test
  void fromSec1AndTransitInfo_compressed_asShortString() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(sec1CompressedBase64 + ":" + transitInfoRaw);
    assertEquals(transitInfoRaw, pseudonymInTransit.transitInfo().asString());
    assertEquals(sec1CompressedBase64, pseudonymInTransit.pseudonym().asShortString());
  }

  @Test
  void fromSec1AndTransitInfo_asString() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(sec1Base64 + ":" + transitInfoRaw);
    assertEquals(transitInfoRaw, pseudonymInTransit.transitInfo().asString());
    assertEquals(sec1Base64, pseudonymInTransit.pseudonym().asString());
  }
}
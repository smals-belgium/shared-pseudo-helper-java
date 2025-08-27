package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;

import be.smals.shared.pseudo.helper.Domain;
import org.junit.jupiter.api.Test;

public class PseudonymInTransitTest {

  private static final String sec1CompressedBase64 = "AgBCRmd68AVWDtBMWajDd2W63E_j7X0WmQdMnz9m3wFkoxH-muD692vlltWjKGCR" +
                                                     "xnftuT9nAGkmDRomoHZQc8G3hQ";
  private static final String transitInfo = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwia2lkIjoiMjAyMi0xMiIsImF1" +
                                            "ZCI6Imh0dHBzOi8vYXBpLWludC5laGVhbHRoLmZnb3YuYmUvcHNldWRvL3YxL2Rv" +
                                            "bWFpbnMvdWhtZXBfdjEifQ..osrl3KS4nkheJvcJ.pXN4Asfg8RGtsoV529YoFRW" +
                                            "P_XSXUViR-wxuvwYTvN9fMSDksq7qZMmmqDstyGyOidHKHrVvtqB0PFrek71P4K8" +
                                            "Rp0rDuvAc6RC2cbdwV08Ksw6t3Wf72H8c8QDKGKmYb84z_oH8TMnY26cAm0nC2Hb" +
                                            "18H-SXTh8xFXe3DK8y06wx4rAAXFZGsXayloJ6oweux_tvKQ4NSWi3gzhjVi0g-q" +
                                            "WR9TYZNj9NNyU9eeSDk9UsXJ8cugpvStu6oFOCbW4520fl6h5oaJ7Rye3.IEd7uL" +
                                            "w-ICHAwqIzfrjOFw";

  private static final Domain domain = createTestDomain("test", 8);

  @Test
  void asString() {
    final var sec1AndTransitInfo = sec1CompressedBase64 + ":" + transitInfo;
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(sec1AndTransitInfo);
    assertEquals(sec1AndTransitInfo, pseudonymInTransit.asShortString());
  }
}

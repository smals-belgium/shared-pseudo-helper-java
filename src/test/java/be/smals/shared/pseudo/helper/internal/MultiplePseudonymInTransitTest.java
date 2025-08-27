package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import be.smals.shared.pseudo.helper.Domain;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MultiplePseudonymInTransitTest {

  private static final Domain domain = createTestDomain("test", 8);
  private static final String pseudoInTransitRaw = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAx" +
                                                   "MjM0NTY3ODkxMAwAAAAAAAAAAQHjvYbZ2eX0Wdz8UejdWdSmg5HWrHNActe2loX3" +
                                                   "Q2Y02CL_hX1ORsOFn_A6PbMLOXvFLBeE-Ue2-8aj5iSGZbWnag" +
                                                   ":" +
                                                   "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwia2lkIjoiMjAyMi0xMiIsImF1" +
                                                   "ZCI6Imh0dHBzOi8vYXBpLWludC5laGVhbHRoLmZnb3YuYmUvcHNldWRvL3YxL2Rv" +
                                                   "bWFpbnMvdWhtZXBfdjEifQ..osrl3KS4nkheJvcJ.pXN4Asfg8RGtsoV529YoFRW" +
                                                   "P_XSXUViR-wxuvwYTvN9fMSDksq7qZMmmqDstyGyOidHKHrVvtqB0PFrek71P4K8" +
                                                   "Rp0rDuvAc6RC2cbdwV08Ksw6t3Wf72H8c8QDKGKmYb84z_oH8TMnY26cAm0nC2Hb" +
                                                   "18H-SXTh8xFXe3DK8y06wx4rAAXFZGsXayloJ6oweux_tvKQ4NSWi3gzhjVi0g-q" +
                                                   "WR9TYZNj9NNyU9eeSDk9UsXJ8cugpvStu6oFOCbW4520fl6h5oaJ7Rye3.IEd7uL" +
                                                   "w-ICHAwqIzfrjOFw";

  @Test
  public void create_without_collection_add_10_pseudonyms() {
    final var multiple = new MultiplePseudonymInTransitImpl(domain);
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(pseudoInTransitRaw);
    for (int i = 0; i < 10; i++) {
      multiple.add(pseudonymInTransit);
    }
    assertEquals(10, multiple.size());
  }

  @Test()
  public void create_without_collection_add_11_pseudonyms() {
    final var multiple = new MultiplePseudonymInTransitImpl(domain);
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(pseudoInTransitRaw);
    for (int i = 0; i < 10; i++) {
      multiple.add(pseudonymInTransit);
    }
    assertEquals(10, multiple.size());
    assertThrows(IllegalArgumentException.class, () -> multiple.add(pseudonymInTransit));
  }

  @Test
  public void create_with_collection_containing_10_pseudonyms() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(pseudoInTransitRaw);
    final var pseudonymInTransitList = List.of(pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit);
    final var multiple = new MultiplePseudonymInTransitImpl(domain, pseudonymInTransitList);
    assertEquals(10, multiple.size());
  }

  @Test
  public void create_with_collection_containing_11_pseudonyms() {
    final var pseudonymInTransit = domain.pseudonymInTransitFactory().fromSec1AndTransitInfo(pseudoInTransitRaw);
    final var pseudonymInTransitList = List.of(pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit, pseudonymInTransit);
    assertThrows(IllegalArgumentException.class, () -> new MultiplePseudonymInTransitImpl(domain, pseudonymInTransitList));
  }
}

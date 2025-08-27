package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import be.smals.shared.pseudo.helper.Domain;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MultiplePseudonymTest {

  private static final Domain domain = createTestDomain("test", 8);

  private static final String X = "AMal20TDNJRgr76LBTeOscce8yCtdG4dJ8GgPge29MEeK0RH5sC5TZ3UTEe2y+0YBoX6ooWODEFuN24FDTzW2UIo";
  private static final String Y = "AVKesmomj6qNSJpqPxEaIegblCAzf8k4gh0V7h/NxrgJ5WDUIha39yfdNeX1maUWrRBwiCNztHkE/ugNiRfFq5WU";

  @Test
  public void create_without_collection_add_10_pseudonyms() {
    final var multiple = new MultiplePseudonymImpl(domain);
    final var pseudonym = domain.pseudonymFactory().fromXY(X, Y);
    for (int i = 0; i < 10; i++) {
      multiple.add(pseudonym);
    }
    assertEquals(10, multiple.size());
  }

  @Test()
  public void create_without_collection_add_11_pseudonyms() {
    final var multiple = new MultiplePseudonymImpl(domain);
    final var pseudonym = domain.pseudonymFactory().fromXY(X, Y);
    for (int i = 0; i < 10; i++) {
      multiple.add(pseudonym);
    }
    assertEquals(10, multiple.size());
    assertThrows(IllegalArgumentException.class, () -> multiple.add(pseudonym));
  }

  @Test
  public void create_with_collection_containing_10_pseudonyms() {
    final var pseudonym = domain.pseudonymFactory().fromXY(X, Y);
    final var pseudonymList = List.of(pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym);
    final var multiple = new MultiplePseudonymImpl(domain, pseudonymList);
    assertEquals(10, multiple.size());
  }

  @Test
  public void create_with_collection_containing_11_pseudonyms() {
    final var pseudonym = domain.pseudonymFactory().fromXY(X, Y);
    final var pseudonymList = List.of(pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym, pseudonym);
    assertThrows(IllegalArgumentException.class, () -> new MultiplePseudonymImpl(domain, pseudonymList));
  }
}

package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import be.smals.shared.pseudo.helper.Domain;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MultipleValueTest {

  private static final Domain domain = createTestDomain("test", 8);

  @Test
  public void create_without_collection_add_10_values() {
    final var multiple = new MultipleValueImpl(domain);
    final var value = domain.valueFactory().from("0");
    for (int i = 0; i < 10; i++) {
      multiple.add(value);
    }
    assertEquals(10, multiple.size());
  }

  @Test()
  public void create_without_collection_add_11_values() {
    final var multiple = new MultipleValueImpl(domain);
    final var value = domain.valueFactory().from("0");
    for (int i = 0; i < 10; i++) {
      multiple.add(value);
    }
    assertEquals(10, multiple.size());
    assertThrows(IllegalArgumentException.class, () -> multiple.add(value));
  }

  @Test
  public void create_with_collection_containing_10_values() {
    final var value = domain.valueFactory().from("0");
    final var valueList = List.of(value, value, value, value, value, value, value, value, value, value);
    final var multiple = new MultipleValueImpl(domain, valueList);
    assertEquals(10, multiple.size());
  }

  @Test
  public void create_with_collection_containing_11_values() {
    final var value = domain.valueFactory().from("0");
    final var valueList = List.of(value, value, value, value, value, value, value, value, value, value, value);
    assertThrows(IllegalArgumentException.class, () -> new MultipleValueImpl(domain, valueList));
  }
}

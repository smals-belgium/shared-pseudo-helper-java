package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import be.smals.shared.pseudo.helper.Domain;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PseudonymFactoryTest {

  private static final Domain domain = createTestDomain("test", 8);
  private static final String sec1Compressed = "AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAxMjM0NTY3ODkxMAwAAAAAAAAAAQ";
  private static final String sec1 = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAxMjM0NTY3ODkxMAwAAAAAAAAAAQAcQnkmJhoLpiMDrhcipitZfG4pU4y_jShJaXoIvJnLJ90AeoKxuTx6YA_Fwkz0xoQ60-h7BrhJBDlcGdt5mkpYlQ";
  private static final String x = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMDEyMzQ1Njc4OTEwDAAAAAAAAAAB";
  private static final String xShortened = "MDEyMzQ1Njc4OTEwDAAAAAAAAAAB";
  private static final String y = "ABxCeSYmGgumIwOuFyKmK1l8bilTjL+NKElpegi8mcsn3QB6grG5PHpgD8XCTPTGhDrT6HsGuEkEOVwZ23maSliV";

  @Test
  public void fromXY_asShortString() {
    final var pseudonym = domain.pseudonymFactory().fromXY(x, y);
    assertEquals(sec1Compressed, pseudonym.asShortString());
  }

  @Test
  public void fromXY_asString() {
    final var pseudonym = domain.pseudonymFactory().fromXY(x, y);
    assertEquals(sec1, pseudonym.asString());
  }

  @Test
  public void fromX() {
    final var pseudonym = domain.pseudonymFactory().fromX(x);
    assertEquals(y, pseudonym.y());
  }

  @Test
  public void fromX_small() {
    final var pseudonym = domain.pseudonymFactory().fromX(xShortened);
    assertEquals(y, pseudonym.y());
  }

  @Test
  public void multiple_no_collection_add_10_pseudonyms() {
    final var multiple = domain.pseudonymFactory().multiple();
    assertEquals(0, multiple.size());
  }

  @Test
  public void multiple_with_collection_containing_10_pseudonyms() {
    final var pseudonym1 = domain.pseudonymFactory().fromXY(x, y);
    final var pseudonym2 = domain.pseudonymFactory().fromXY(x, y);
    final var pseudonymList = List.of(pseudonym1, pseudonym2);
    final var multiple = domain.pseudonymFactory().multiple(pseudonymList);
    assertEquals(2, multiple.size());
    assertSame(multiple.get(0), pseudonym1);
    assertSame(multiple.get(1), pseudonym2);
  }
}

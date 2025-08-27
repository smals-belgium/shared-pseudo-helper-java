package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.TestUtils.createTestDomain;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import be.smals.shared.pseudo.helper.Domain;
import java.math.BigInteger;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class PseudonymTest {

  private static final Domain domain = createTestDomain("test", 8);
  private static final String ONE_B64 = Base64.getEncoder().encodeToString(ONE.toByteArray());
  private static final String TWO_B64 = Base64.getEncoder().encodeToString(TWO.toByteArray());

  @Test
  void xBase64ShouldReturnXEncodedBase64() {
    final var pseudonym = domain.pseudonymFactory().fromXY(ONE_B64, TWO_B64);
    final var output = pseudonym.x();
    assertEquals(ONE, new BigInteger(Base64.getDecoder().decode(output)));
  }

  @Test
  void yBase64ShouldReturnYEncodedBase64() {
    final var pseudonym = domain.pseudonymFactory().fromXY(ONE_B64, TWO_B64);
    assertEquals(TWO, new BigInteger(Base64.getDecoder().decode(pseudonym.y())));
  }

  @Test
  void fromXY_asShortString() {
    final var sec1 = "AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAxMjM0NTY3ODkxMAwAAAAAAAAAAQ";
    final String x = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMDEyMzQ1Njc4OTEwDAAAAAAAAAAB";
    final String y = "ABxCeSYmGgumIwOuFyKmK1l8bilTjL+NKElpegi8mcsn3QB6grG5PHpgD8XCTPTGhDrT6HsGuEkEOVwZ23maSliV";
    final var pseudonym = domain.pseudonymFactory().fromXY(x, y);
    assertEquals(sec1, pseudonym.asShortString());
  }
}
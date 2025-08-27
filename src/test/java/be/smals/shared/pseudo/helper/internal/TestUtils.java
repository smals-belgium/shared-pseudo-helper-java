package be.smals.shared.pseudo.helper.internal;

import org.bouncycastle.jce.ECNamedCurveTable;

public class TestUtils {

  public static DomainImpl createTestDomain(final String key, final int bufferSize) {
    return new DomainImpl(key, null, ECNamedCurveTable.getParameterSpec("P-521").getCurve(), null, bufferSize, null, null, null, null, null, null);
  }
}

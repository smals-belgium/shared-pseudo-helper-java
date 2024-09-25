package be.smals.shared.pseudo.helper;

@SuppressWarnings("unused")
public interface Domain {

  // tag::methods[]
  /**
   * Returns the key of this domain.
   *
   * @return the key of this domain
   */
  String key();

  /**
   * Returns the {@link ValueFactory} of this domain.
   *
   * @return the {@link ValueFactory} of this domain
   */
  ValueFactory valueFactory();

  /**
   * Returns the {@link PseudonymFactory} of this domain.
   *
   * @return the {@link PseudonymFactory} of this domain
   */
  PseudonymFactory pseudonymFactory();

  /**
   * Returns the {@link PseudonymInTransitFactory} of this domain.
   *
   * @return the {@link PseudonymInTransitFactory} of this domain
   */
  PseudonymInTransitFactory pseudonymInTransitFactory();
  // end::methods[]
}

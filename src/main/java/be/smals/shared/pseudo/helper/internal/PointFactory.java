package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.Domain;

public abstract class PointFactory {

  protected final DomainImpl domain;

  protected PointFactory(final Domain domain) {
    this.domain = (DomainImpl) domain;
  }
}

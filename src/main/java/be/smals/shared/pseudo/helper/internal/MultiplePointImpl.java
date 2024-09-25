package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.MultiplePoint;
import be.smals.shared.pseudo.helper.Point;
import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class MultiplePointImpl<T extends Point> implements MultiplePoint<T> {

  protected final DomainImpl domain;
  protected final ArrayList<Object> points;

  MultiplePointImpl(final DomainImpl domain) {
    this.domain = domain;
    this.points = new ArrayList<>(10);
  }

  MultiplePointImpl(final DomainImpl domain, final Collection<?> points) {
    checkCollectionSize(points.size());
    this.domain = domain;
    this.points = new ArrayList<>(points);
  }

  @Override
  public DomainImpl domain() {
    return domain;
  }

  @Override
  public int size() {
    return points.size();
  }

  @Override
  public boolean add(final T point) {
    checkCollectionSize(points.size() + 1);
    return points.add(validate(point));
  }

  void add(final EHealthProblemImpl problem) {
    checkCollectionSize(points.size() + 1);
    points.add(problem);
  }

  @Override
  public T get(final int index) throws EHealthProblemException {
    final var pointOrProblem = points.get(index);
    if (pointOrProblem instanceof EHealthProblemImpl) {
      throw new EHealthProblemException((EHealthProblemImpl) pointOrProblem);
    }
    //noinspection unchecked
    return (T) pointOrProblem;
  }

  /**
   * Checks if given size is a valid size.
   *
   * @param size the size to validate
   */
  abstract void checkCollectionSize(final int size);

  /**
   * Validate if the given point can be added.
   *
   * @param point the {@link Point} to validate
   * @return the given {@link Point}
   */
  abstract T validate(final T point);
}

package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;

public interface MultiplePoint<T extends Point> {

  /**
   * Returns the {@link Domain} to which this collection belong.
   *
   * @return the {@link Domain} to which this collection belong
   */
  Domain domain();

  /**
   * Returns the number of elements.
   * <p>
   * The size will always be &gt;= 0 and &lt;= 10.
   *
   * @return the number of elements in this collection
   */
  int size();

  /**
   * Add an element.
   *
   * @param t element to add
   * @return {@code true} if the given item has been added
   */
  boolean add(T t);

  /**
   * Returns the element at the given index.
   * <p>
   * If the eHealth response for the element at this index was a problem,
   * then a {@link EHealthProblemException} is thrown.
   *
   * @param index the index of the element to return
   * @return the element at the given index
   */
  T get(int index) throws EHealthProblemException;
}

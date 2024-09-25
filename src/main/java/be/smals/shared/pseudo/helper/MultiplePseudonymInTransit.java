package be.smals.shared.pseudo.helper;

import java.util.concurrent.CompletableFuture;

/**
 * Collection of {@link PseudonymInTransit}s, all belonging to the same {@link Domain}.
 * <p>
 * You cannot put more than 10 {@link PseudonymInTransit}s in this collection.
 */
public interface MultiplePseudonymInTransit extends MultiplePoint<PseudonymInTransit> {

  /**
   * Convert all the {@link PseudonymInTransit}s of this collection into {@link PseudonymInTransit}s of the given domain.
   * <p>
   * Please note that the {@link PseudonymInTransit}s will be returned in the order eHealth returned it
   * (it means that the first {@link PseudonymInTransit} is linked to the first {@link PseudonymInTransit} you gave, the second one to the second one,...).
   *
   * @param toDomain the target {@link Domain} for the returned {@link PseudonymInTransit}s
   * @return a {@link CompletableFuture} of {@link MultiplePseudonymInTransit}
   * that contains the converted {@link PseudonymInTransit}s for the given {@link Domain}.
   */
  @SuppressWarnings("unused")
  CompletableFuture<? extends MultiplePseudonymInTransit> convertTo(Domain toDomain);

  /**
   * Identify (de-pseudonymise) the {@link PseudonymInTransit}s of this collection.
   *
   * @return a {@link CompletableFuture} of {@link MultipleValue} containing the identified {@link PseudonymInTransit}s of this collection
   */
  @SuppressWarnings("unused")
  CompletableFuture<? extends MultipleValue> identify();
}

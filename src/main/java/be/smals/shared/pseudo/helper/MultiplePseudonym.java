package be.smals.shared.pseudo.helper;

import java.util.concurrent.CompletableFuture;

/**
 * Collection of {@link Pseudonym}s, all belonging to the same {@link Domain}.
 * <p>
 * You cannot put more than 10 {@link Pseudonym}s in this collection.
 */
public interface MultiplePseudonym extends MultiplePoint<Pseudonym> {

  /**
   * Convert all the {@link Pseudonym}s of this collection into {@link PseudonymInTransit}s of the given domain.
   * <p>
   * Please note that the {@link PseudonymInTransit}s will be returned in the order eHealth returned it
   * (it means that the first {@link PseudonymInTransit} is linked to the first {@link Pseudonym} you gave, the second one to the second one,...).
   *
   * @param toDomain the key of the target {@link Domain} for the returned {@link PseudonymInTransit}s
   * @return a {@link CompletableFuture} of {@link MultiplePseudonymInTransit} that contains the converted {@link Pseudonym}s for the given {@link Domain}.
   */
  @SuppressWarnings("unused")
  CompletableFuture<? extends MultiplePseudonymInTransit> convertTo(Domain toDomain);
}

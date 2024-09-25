package be.smals.shared.pseudo.helper;

import java.util.concurrent.CompletableFuture;

/**
 * Collection of {@link Value}s, all belonging to the same {@link Domain}.
 * <p>
 * You cannot put more than 10 {@link Value}s in this collection.
 */
public interface MultipleValue extends MultiplePoint<Value> {

  /**
   * Pseudonymise all the {@link Value}s of this collection into {@link PseudonymInTransit}s of the same domain.
   * <p>
   * Please note that the {@link PseudonymInTransit}s will be returned in the order eHealth returned it
   * (it means that the first {@link PseudonymInTransit} is linked to the first {@link Value} you gave, the second one to the second one,...).
   *
   * @return a {@link CompletableFuture} of {@link MultiplePseudonymInTransit} that contains the pseudonymised values.
   */
  @SuppressWarnings("unused")
  CompletableFuture<? extends MultiplePseudonymInTransit> pseudonymize();
}

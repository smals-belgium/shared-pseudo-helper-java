package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.util.concurrent.CompletableFuture.completedFuture;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.MultiplePseudonym;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MultiplePseudonymImpl extends MultiplePointImpl<Pseudonym> implements MultiplePseudonym {

  MultiplePseudonymImpl(final Domain domain) {
    super((DomainImpl) domain);
  }

  MultiplePseudonymImpl(final Domain domain, final Collection<Pseudonym> pseudonyms) {
    super((DomainImpl) domain, pseudonyms);
  }

  @Override
  public CompletableFuture<MultiplePseudonymInTransitImpl> convertTo(final Domain toDomain) {

    if (points.isEmpty()) {
      return completedFuture(new MultiplePseudonymInTransitImpl(domain));
    }

    final var nbPseudonyms = points.size();
    if (nbPseudonyms == 1) {
      @SuppressWarnings("SuspiciousToArrayCall")
      final var pseudonym = points.toArray(new Pseudonym[1])[0];
      return pseudonym.convertTo(toDomain)
                      .thenApply(pseudonymInTransit -> new MultiplePseudonymInTransitImpl(toDomain, List.of(pseudonymInTransit)))
                      .exceptionally(throwable -> {
                        if (throwable instanceof EHealthProblemException) {
                          return new MultiplePseudonymInTransitImpl(toDomain, List.of(((EHealthProblemException) throwable).getProblem()));
                        }
                        return throwWrapped(throwable);
                      });
    }

    final var randoms = new ArrayList<BigInteger>(nbPseudonyms);
    final var payload = new JsonObject();
    final var inputs = new JsonArray();
    payload.add("inputs", inputs);
    for (int i = 0; i < nbPseudonyms; i++) {
      final var random = domain.createRandom();
      inputs.add(domain.createPayload(((PseudonymImpl) points.get(i)).multiply(random)));
      randoms.add(random);
    }
    return domain.pseudonymisationClient()
                 .convertMultipleTo(domain.key(), toDomain.key(), payload.toString())
                 .thenApply(rawResponse -> {
                   final var response = JSonHelper.parse(rawResponse);
                   final var outputs = (JsonArray) response.get("outputs");
                   final var pseudonymsInTransit = new MultiplePseudonymInTransitImpl(toDomain);
                   final var pseudonymInTransitFactory = (PseudonymInTransitFactoryImpl) toDomain.pseudonymInTransitFactory();
                   for (int i = 0; i < nbPseudonyms; i++) {
                     try {
                       pseudonymsInTransit.add(pseudonymInTransitFactory.fromResponse((JsonObject) outputs.get(i), randoms.get(i)));
                     } catch (final EHealthProblemException e) {
                       pseudonymsInTransit.add((EHealthProblemImpl) e.getProblem());
                     }
                   }
                   return pseudonymsInTransit;
                 });
  }

  @Override
  void checkCollectionSize(final int size) {
    if (size > 10) {
      throw new IllegalArgumentException("The number of pseudonyms in this collection must be less or equal to 10");
    }
  }

  @Override
  Pseudonym validate(final Pseudonym pseudonym) {
    // Ensures that the pseudonym is from the expected domain
    if (!pseudonym.domain().key().equals(domain.key())) {
      throw new IllegalArgumentException("All given pseudonyms are not from the domain `" + domain.key() + "`");
    }
    // Ensures that the pseudonym is not instance of PseudonymInTransit
    if (pseudonym instanceof PseudonymInTransit) {
      throw new IllegalArgumentException("None of the provided pseudonyms can be `PseudonymInTransit`");
    }
    return pseudonym;
  }
}

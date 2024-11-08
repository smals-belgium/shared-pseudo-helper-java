package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.util.concurrent.CompletableFuture.completedFuture;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.MultiplePseudonymInTransit;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MultiplePseudonymInTransitImpl extends MultiplePointImpl<PseudonymInTransit> implements MultiplePseudonymInTransit {

  MultiplePseudonymInTransitImpl(final Domain domain) {
    super((DomainImpl) domain);
  }

  MultiplePseudonymInTransitImpl(final Domain domain, final Collection<?> pseudonymsInTransit) {
    super((DomainImpl) domain, pseudonymsInTransit);
  }

  @Override
  public CompletableFuture<MultiplePseudonymInTransitImpl> convertTo(final Domain toDomain) {

    if (points.isEmpty()) {
      return completedFuture(new MultiplePseudonymInTransitImpl(domain));
    }

    final var nbPseudonymsInTransit = points.size();
    if (nbPseudonymsInTransit == 1) {
      @SuppressWarnings("SuspiciousToArrayCall")
      final var pseudonymInTransitToConvert = points.toArray(new PseudonymInTransit[1])[0];
      return pseudonymInTransitToConvert
                 .convertTo(toDomain)
                 .thenApply(pseudonymInTransit -> new MultiplePseudonymInTransitImpl(toDomain, List.of(pseudonymInTransit)))
                 .exceptionally(throwable -> {
                   if (throwable instanceof EHealthProblemException) {
                     return new MultiplePseudonymInTransitImpl(toDomain, List.of(((EHealthProblemException) throwable).getProblem()));
                   }
                   return throwWrapped(throwable);
                 });
    }

    final var randoms = new ArrayList<BigInteger>(nbPseudonymsInTransit);
    final var payload = new JsonObject();
    final var inputs = new JsonArray();
    payload.add("inputs", inputs);
    for (int i = 0; i < nbPseudonymsInTransit; i++) {
      final var random = domain.createRandom();
      final var pseudonymInTransit = (PseudonymInTransitImpl) points.get(i);
      inputs.add(domain.createPayload(pseudonymInTransit.pseudonym().multiply(random), pseudonymInTransit.transitInfo().asString()));
      randoms.add(random);
    }
    return domain.pseudonymisationClient()
                 .convertMultipleTo(domain.key(), toDomain.key(), payload.toString())
                 .thenApply(rawResponse -> {
                   final var response = JSonHelper.parse(rawResponse);
                   final var outputs = (JsonArray) response.get("outputs");
                   final var pseudonymsInTransit = new MultiplePseudonymInTransitImpl(toDomain);
                   final var pseudonymInTransitFactory = (PseudonymInTransitFactoryImpl) toDomain.pseudonymInTransitFactory();
                   for (int i = 0; i < nbPseudonymsInTransit; i++) {
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
  public CompletableFuture<MultipleValueImpl> identify() {

    if (points.isEmpty()) {
      return completedFuture(new MultipleValueImpl(domain));
    }

    final var nbPseudonymsInTransit = points.size();
    if (nbPseudonymsInTransit == 1) {
      @SuppressWarnings("SuspiciousToArrayCall")
      final var pseudonymInTransitToIdentify = points.toArray(new PseudonymInTransit[0])[0];
      return pseudonymInTransitToIdentify
                 .identify()
                 .thenApply(value -> new MultipleValueImpl(domain, List.of(value)))
                 .exceptionally(throwable -> {
                   if (throwable instanceof EHealthProblemException) {
                     return new MultipleValueImpl(domain, List.of(((EHealthProblemException) throwable).getProblem()));
                   }
                   return throwWrapped(throwable);
                 });
    }

    final var randoms = new ArrayList<BigInteger>(nbPseudonymsInTransit);
    final var payload = new JsonObject();
    final var inputs = new JsonArray();
    payload.add("inputs", inputs);
    for (int i = 0; i < nbPseudonymsInTransit; i++) {
      final var random = domain.createRandom();
      final var pseudonymInTransit = (PseudonymInTransitImpl) points.get(i);
      inputs.add(domain.createPayload(pseudonymInTransit.pseudonym().multiply(random), pseudonymInTransit.transitInfo().asString()));
      randoms.add(random);
    }
    return domain.pseudonymisationClient()
                 .identifyMultiple(domain.key(), payload.toString())
                 .thenApply(rawResponse -> {
                   final var response = JSonHelper.parse(rawResponse);
                   final var outputs = (JsonArray) response.get("outputs");
                   final var values = new MultipleValueImpl(domain);
                   final var pseudonymFactory = domain.pseudonymFactory();
                   for (int i = 0; i < nbPseudonymsInTransit; i++) {
                     try {
                       values.add(pseudonymFactory.fromResponse((JsonObject) outputs.get(i), randoms.get(i)).asValue());
                     } catch (final EHealthProblemException e) {
                       values.add((EHealthProblemImpl) e.getProblem());
                     }
                   }
                   return values;
                 });
  }

  @Override
  void checkCollectionSize(final int size) {
    if (size > 10) {
      throw new IllegalArgumentException("The number of pseudonyms in transit in this collection must be less or equal to 10");
    }
  }

  @Override
  PseudonymInTransit validate(final PseudonymInTransit pseudonymInTransit) {
    // Ensures that the pseudonym in transit is from the expected domain
    if (!pseudonymInTransit.domain().key().equals(domain.key())) {
      throw new IllegalArgumentException("All given pseudonyms in transit are not from the domain `" + domain.key() + "`");
    }
    return pseudonymInTransit;
  }
}

package be.smals.shared.pseudo.helper.internal;

import static java.util.concurrent.CompletableFuture.completedFuture;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.MultipleValue;
import be.smals.shared.pseudo.helper.Value;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MultipleValueImpl extends MultiplePointImpl<Value> implements MultipleValue {

  MultipleValueImpl(final Domain domain) {
    super((DomainImpl) domain);
  }

  MultipleValueImpl(final Domain domain, final Collection<?> values) {
    super((DomainImpl) domain, values);
  }

  @Override
  public CompletableFuture<MultiplePseudonymInTransitImpl> pseudonymize() {

    if (points.isEmpty()) {
      return completedFuture(new MultiplePseudonymInTransitImpl(domain));
    }

    final var nbValues = points.size();
    if (nbValues == 1) {
      @SuppressWarnings("SuspiciousToArrayCall")
      final var value = points.toArray(new Value[1])[0];
      return value.pseudonymize()
                  .thenApply(pseudonymInTransit -> new MultiplePseudonymInTransitImpl(domain, List.of(pseudonymInTransit)));
    }

    final var randoms = new ArrayList<BigInteger>(nbValues);
    final var payload = new JsonObject();
    final var inputs = new JsonArray();
    payload.add("inputs", inputs);
    for (int i = 0; i < nbValues; i++) {
      final var random = domain.createRandom();
      inputs.add(domain.createPayload(((ValueImpl) points.get(i)).multiply(random)));
      randoms.add(random);
    }
    return domain.pseudonymisationClient()
                 .pseudonymizeMultiple(domain.key(), payload.toString())
                 .thenApply(rawResponse -> {
                   final var response = JSonHelper.parse(rawResponse);
                   final var outputs = (JsonArray) response.get("outputs");
                   final var pseudonymsInTransit = new MultiplePseudonymInTransitImpl(domain);
                   final var pseudonymInTransitFactory = domain.pseudonymInTransitFactory();
                   for (int i = 0; i < nbValues; i++) {
                     final var output = (JsonObject) outputs.get(i);
                     if (pseudonymInTransitFactory.isAcceptableResponse(output)) {
                       pseudonymsInTransit.add(pseudonymInTransitFactory.fromResponse(output, randoms.get(i)));
                     } else {
                       pseudonymsInTransit.add(EHealthProblemImpl.fromResponse(output));
                     }
                   }
                   return pseudonymsInTransit;
                 });
  }

  @Override
  protected void checkCollectionSize(final int size) {
    if (size > 10) {
      throw new IllegalArgumentException("The number of values in this collection must be less or equal to 10");
    }
  }

  @Override
  Value validate(final Value value) {
    // Ensures that the value is from the expected domain
    if (!value.domain().key().equals(domain.key())) {
      throw new IllegalArgumentException("All given values are not from the domain `" + domain.key() + "`");
    }
    return value;
  }
}

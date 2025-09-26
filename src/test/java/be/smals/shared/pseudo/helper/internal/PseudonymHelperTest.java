package be.smals.shared.pseudo.helper.internal;

import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.shaded.gson.JsonParser.parseString;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.MultiplePseudonymInTransit;
import be.smals.shared.pseudo.helper.MultipleValue;
import be.smals.shared.pseudo.helper.PrivateKeySupplier;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.PseudonymisationClient;
import be.smals.shared.pseudo.helper.PseudonymisationHelper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PseudonymHelperTest {

  private PseudonymisationHelper pseudonymisationHelper;

  private static final String domain;
  private static final RSAKey rsaKey;
  private static final URI jwksUrl;

  static {
    try {
      var pem = new String(getSystemResourceAsStream("1757512889.pem").readAllBytes(), UTF_8);
      var publicSimpleKey = RSAKey.parseFromPEMEncodedX509Cert(pem);
      rsaKey = new RSAKey.Builder((RSAKey) RSAKey.parseFromPEMEncodedX509Cert(pem))
                   .privateKey(((RSAKey) RSAKey.parseFromPEMEncodedObjects(pem)).toRSAPrivateKey())
                   .algorithm(RSA_OAEP_256)
                   .build();
      domain = new String(getSystemResourceAsStream("domain.json").readAllBytes(), UTF_8);
      jwksUrl = URI.create("https://my-jwks.net/" + publicSimpleKey.getKeyID());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final PrivateKeySupplier privateKeySupplier = hash -> {
    try {
      if (hash.equals(rsaKey.getX509CertSHA256Thumbprint().toString())) {
        return rsaKey.toRSAPrivateKey();
      }
      return null;
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
  };

  @BeforeEach
  void setup() {
    Supplier<CompletableFuture<String>> jwksSupplier = () -> completedFuture(new JWKSet(rsaKey).toString(true));

    PseudonymisationHelper internalPseudonymisationHelper =
        PseudonymisationHelper.builder()
                              .jwksUrl(jwksUrl)
                              .jwkSupplier(jwksSupplier)
                              .privateKeySupplier(privateKeySupplier)
                              .pseudonymisationClient(domainKey -> completedFuture(domain))
                              .build();
    pseudonymisationHelper =
        PseudonymisationHelper.builder()
                              .jwksUrl(jwksUrl)
                              .privateKeySupplier(privateKeySupplier)
                              .jwkSupplier(jwksSupplier)
                              .pseudonymisationClient(new PseudonymisationClient() {
                                @Override
                                public CompletableFuture<String> getDomain(String domainKey) {
                                  return completedFuture(domain);
                                }

                                @Override
                                public CompletableFuture<String> identify(String domainKey, String payload) {
                                  var now = now();
                                  var pseudo = (JsonObject) parseString(payload);
                                  return internalPseudonymisationHelper
                                             .getDomain("test")
                                             .thenApply(testDom -> {
                                               var x = pseudo.get("x").getAsString();
                                               var y = pseudo.get("y").getAsString();
                                               var transitInfo = pseudo.get("transitInfo").getAsString();
                                               var pseudonymInTransit = testDom.pseudonymInTransitFactory().fromXYAndTransitInfo(x, y, transitInfo);
                                               var pseudonymAtRest = pseudonymInTransit.atRest();
                                               return createPseudo(pseudonymAtRest, null, now).toString();
                                             });
                                }

                                @Override
                                public CompletableFuture<String> identifyMultiple(String domainKey, String payload) {
                                  try {
                                    var domain = internalPseudonymisationHelper.getDomain("test").get();
                                    var now = now();
                                    var response = new JsonObject();
                                    var outputs = new JsonArray();
                                    response.add("outputs", outputs);
                                    ((JsonArray) ((JsonObject) parseString(payload)).get("inputs"))
                                        .asList().stream()
                                        .map(input -> (JsonObject) input)
                                        .map(pseudo -> {
                                          var x = pseudo.get("x").getAsString();
                                          var y = pseudo.get("y").getAsString();
                                          var transitInfo = pseudo.get("transitInfo").getAsString();
                                          var pseudonymInTransit = domain.pseudonymInTransitFactory().fromXYAndTransitInfo(x, y, transitInfo);
                                          var pseudonym = pseudonymInTransit.atRest();
                                          return createPseudo(pseudonym, pseudo.get("id").getAsString(), now);
                                        })
                                        .forEach(outputs::add);
                                    return completedFuture(response.toString());
                                  } catch (Exception e) {
                                    throw new RuntimeException(e);
                                  }
                                }

                                @Override
                                public CompletableFuture<String> pseudonymize(String domainKey, String payload) {
                                  var now = now();
                                  var jsonObject = (JsonObject) parseString(payload);
                                  return internalPseudonymisationHelper
                                             .getDomain("test")
                                             .thenApply(domain -> {
                                               var pseudonym = domain.pseudonymFactory()
                                                                     .fromXY(jsonObject.get("x").getAsString(), jsonObject.get("y").getAsString());
                                               var pseudonymInTransit = pseudonym.inTransit();
                                               return createPseudo(pseudonymInTransit, null, now).toString();
                                             });
                                }

                                @Override
                                public CompletableFuture<String> pseudonymizeMultiple(String domainKey, String payload) {
                                  try {
                                    var domain = internalPseudonymisationHelper.getDomain("test").get();
                                    var now = now();
                                    var response = new JsonObject();
                                    var outputs = new JsonArray();
                                    response.add("outputs", outputs);
                                    ((JsonArray) ((JsonObject) parseString(payload)).get("inputs"))
                                        .asList().stream()
                                        .map(input -> (JsonObject) input)
                                        .map(pseudo -> {
                                          final var pseudonym = domain.pseudonymFactory().fromXY(pseudo.get("x").getAsString(), pseudo.get("y").getAsString());
                                          return createPseudo(pseudonym.inTransit(), pseudo.get("id").getAsString(), now);
                                        })
                                        .forEach(outputs::add);
                                    return completedFuture(response.toString());
                                  } catch (Exception e) {
                                    throw new RuntimeException(e);
                                  }
                                }

                                @Override
                                public CompletableFuture<String> convertTo(String fromDomainKey, String toDomainKey, String payload) {
                                  return null;
                                }

                                private JsonObject createPseudo(Pseudonym pseudonym, String inResponseTo, Instant now) {
                                  return createPseudo(pseudonym.domain().key(),
                                                      pseudonym.x(), pseudonym.y(), null,
                                                      inResponseTo, now);
                                }

                                private JsonObject createPseudo(PseudonymInTransit pseudonymInTransit, String inResponseTo, Instant now) {
                                  return createPseudo(pseudonymInTransit.pseudonym().domain().key(),
                                                      pseudonymInTransit.x(), pseudonymInTransit.y(), pseudonymInTransit.transitInfo().asString(),
                                                      inResponseTo, now);
                                }

                                private JsonObject createPseudo(String domainKey,
                                                                String x,
                                                                String y,
                                                                String transitInfo,
                                                                String inResponseTo,
                                                                Instant now) {
                                  var response = new JsonObject();
                                  response.add("id", new JsonPrimitive(UUID.randomUUID().toString()));
                                  response.add("domain", new JsonPrimitive(domainKey));
                                  response.add("crv", new JsonPrimitive("P-521"));
                                  response.add("iat", new JsonPrimitive(now.getEpochSecond()));
                                  response.add("exp", new JsonPrimitive(now.plus(1, HOURS).getEpochSecond()));
                                  response.add("x", new JsonPrimitive(x));
                                  response.add("y", new JsonPrimitive(y));
                                  if (transitInfo != null) {
                                    response.add("transitInfo", new JsonPrimitive(transitInfo));
                                  }
                                  if (inResponseTo != null) {
                                    response.add("inResponseTo", new JsonPrimitive(inResponseTo));
                                  }
                                  return response;
                                }
                              })
                              .build();
  }

  @Test
  public void test() throws ExecutionException, InterruptedException {
    var ssin = "01234567890";
    var value = pseudonymisationHelper.getDomain("test")
                                      .thenCompose(domain -> domain.valueFactory().from(ssin).pseudonymize())
                                      .thenCompose(PseudonymInTransit::identify)
                                      .get();
    assertEquals(ssin, value.asString());
  }

  @Test
  public void test_multiple() throws ExecutionException, InterruptedException {
    Domain domain = pseudonymisationHelper.getDomain("test").get();
    var valueFactory = domain.valueFactory();
    var values = Stream.of("01234567890", "12345678901", "23456789012")
                       .map(valueFactory::from)
                       .collect(toList());
    MultipleValue identifiedValues = valueFactory.multiple(values).pseudonymize().thenCompose(MultiplePseudonymInTransit::identify).get();
    assertEquals(values.size(), identifiedValues.size());
    for (int i = 0; i < identifiedValues.size(); i++) {
      assertEquals(values.get(i).asString(), identifiedValues.get(i).asString());
    }
  }
}
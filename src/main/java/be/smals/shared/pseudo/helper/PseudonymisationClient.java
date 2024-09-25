package be.smals.shared.pseudo.helper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Implement this interface to allow the {@link PseudonymisationHelper} to call the eHealth Pseudonymisation service.
 * <p>
 * Do not forget to add the following headers in each request:
 * <ul>
 *   <li>Content-Type: 'application/json'</li>
 *   <li>From: see eHealth Pseudonymisation cookbook</li>
 *   <li>User-Agent: see eHealth Pseudonymisation cookbook</li>
 * </ul>
 */
@SuppressWarnings("unused")
public interface PseudonymisationClient {

  // tag::methods[]
  /**
   * Calls /pseudo/v1/domains/{domainKey} and returns a {@link Future} of the response as a String.
   * <p>
   * Each call to this method <strong>must</strong> make a call to eHealth pseudonymisation service: please do not return a cached response !
   *
   * @param domainKey the domain key
   * @return the response as a String
   */
  CompletableFuture<String> getDomain(String domainKey);

  /**
   * Calls /pseudo/v1/domains/{domainKey}/identify with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param domainKey the domain key
   * @param payload   the request body
   * @return the response as a String
   */
  default CompletableFuture<String> identify(final String domainKey, final String payload) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls /pseudo/v1/domains/{domainKey}/identifyMultiple with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param domainKey the domain key
   * @param payload   the request body
   * @return the response as a String
   */
  default CompletableFuture<String> identifyMultiple(final String domainKey, final String payload) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls /pseudo/v1/domains/{domainKey}/pseudonymize with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param domainKey the domain key
   * @param payload   the request body
   * @return the response as a String
   */
  default CompletableFuture<String> pseudonymize(final String domainKey, final String payload) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls /pseudo/v1/domains/{domainKey}/pseudonymizeMultiple with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param domainKey the domain key
   * @param payload   the request body
   * @return the response as a String
   */
  default CompletableFuture<String> pseudonymizeMultiple(final String domainKey, final String payload) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls /pseudo/v1/domains/{fromDomainKey}/convertTo/{toDomainKey} with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param fromDomainKey the domain of the pseudonym to convert
   * @param toDomainKey   the target domain
   * @param payload       the request body
   * @return the response as a String
   */
  default CompletableFuture<String> convertTo(final String fromDomainKey, final String toDomainKey, final String payload) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls /pseudo/v1/domains/{fromDomainKey}/convertMultipleTo/{toDomainKey} with the given payload and returns a {@link Future} of the response as a String.
   *
   * @param fromDomainKey the domain of the pseudonym to convert
   * @param toDomainKey   the target domain
   * @param payload       the request body
   * @return the response as a String
   */
  default CompletableFuture<String> convertMultipleTo(final String fromDomainKey, final String toDomainKey, final String payload) {
    throw new UnsupportedOperationException();
  }
  // end::methods[]
}

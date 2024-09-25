package be.smals.shared.pseudo.helper;

import java.security.PrivateKey;

/**
 * Provides the private keys to use to decrypt the secret keys of the domain.
 */
public interface PrivateKeySupplier {

  /**
   * Returns the private key by its no padding Base64 URL SHA-256 hash.
   *
   * @param hash the no padding Base64 URL SHA-256 hash of the private key to return
   * @return The private key matching the given hash or {@code null} of there is no matching private key.
   */
  PrivateKey getByHash(String hash);
}

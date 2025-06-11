package com.dua3.utility.crypt;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.Optional;

/**
 * Utility class for certificate operations.
 */
public final class CertificateUtil {

    /**
     * Utility class private constructor.
     */
    private CertificateUtil() { /* utility class */ }

    /**
     * Creates a self-signed X.509 certificate using the provided key pair, subject distinguished
     * name, and validity period in days. The certificate is generated with the subject name
     * and validity period specified, and it is signed using the private key from the provided key pair.
     *
     * @param keyPair the key pair containing the private key used to sign the certificate
     *                and the public key embedded within the certificate
     * @param subject the distinguished name (DN) of the certificate's subject, formatted as
     *                a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @param validityDays the number of days from the current date for which the certificate
     *                     will be valid
     * @return an array of X.509 certificates, with the first certificate being the newly created
     *         self-signed certificate and any additional certificates in the chain
     * @throws GeneralSecurityException if an error occurs during the certificate generation
     *         or signing process
     */
    public static Certificate[] createSelfSignedX509Certificate(KeyPair keyPair, String subject, int validityDays) throws GeneralSecurityException {
        try {
            Optional<X509CertificateBuilder> builder = X509CertificateBuilder.getBuilder();
            if (builder.isEmpty()) {
                throw new GeneralSecurityException("No X.509 certificate builder available");
            }

            return builder.get()
                    .subject(subject)
                    .validityDays(validityDays)
                    .build(keyPair);
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to create X.509 certificate", e);
        }
    }
}
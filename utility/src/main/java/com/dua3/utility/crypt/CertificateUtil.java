package com.dua3.utility.crypt;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.Certificate;

/**
 * Utility class for certificate operations.
 */
public final class CertificateUtil {

    /**
     * Utility class private constructor.
     */
    private CertificateUtil() {
        // nothing to do
    }

    /**
     * Creates a self-signed X.509 certificate using available security providers.
     * This method searches through all available providers to find one that supports
     * the necessary operations for X.509 certificate generation.
     */
    public static Certificate[] createSelfSignedX509Certificate(KeyPair keyPair, String subject, int validityDays) throws GeneralSecurityException {
        try {
            return X509CertificateBuilder.getBuilder()
                    .subject(subject)
                    .validityDays(validityDays)
                    .build(keyPair);
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to create X.509 certificate", e);
        }
    }
}
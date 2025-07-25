package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
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
     * Creates a self-signed X.509 certificate using the provided key pair, subject distinguished name,
     * validity duration, and a flag to enable Certificate Authority (CA) settings.
     *
     * @param keyPair     the key pair containing the private key used to sign the certificate
     *                    and the public key embedded within the certificate
     * @param subject     the distinguished name (DN) of the certificate's subject, formatted as
     *                    a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @param validityDays the number of days from the current date for which the certificate
     *                     will be valid
     * @param enableCA     a boolean flag indicating whether the certificate should include settings
     *                    for acting as a Certificate Authority (CA). If {@code true}, the certificate
     *                    will be configured as a CA.
     * @return an array of X.509 certificates, with the first certificate being the newly created
     *         self-signed certificate. The array may include additional certificates if the chain
     *         requires it.
     * @throws GeneralSecurityException if an error occurs during the certificate generation
     *                                  or signing process
     */
    public static X509Certificate[] createSelfSignedX509Certificate(KeyPair keyPair, String subject, int validityDays, boolean enableCA) throws GeneralSecurityException {
        try {
            Optional<X509CertificateBuilder> builder = X509CertificateBuilder.getBuilder(enableCA);
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

    /**
     * Creates an X.509 certificate using the provided key pair, subject distinguished name,
     * validity period, and a parent certificate. The generated certificate is included in a chain
     * along with the specified parent certificate(s).
     *
     * @param keyPair           the key pair containing the private key used to sign the certificate
     *                          and the public key embedded within the certificate
     * @param subject           the distinguished name (DN) of the certificate's subject, formatted as
     *                          a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @param validityDays      the number of days from the current date for which the certificate
     *                          will be valid
     * @param parentCertificate the parent X.509 certificate to include in the generated certificate's
     *                          chain, establishing a chain of trust
     * @param parentPrivateKey the parent certificate's private key
     * @return an array of X.509 certificates, with the first certificate being the newly created
     * certificate, followed by the specified parent certificate(s) in the chain
     * @throws GeneralSecurityException if an error occurs during the certificate generation
     *                                  or signing process
     */
    public static X509Certificate[] createX509Certificate(KeyPair keyPair, String subject, int validityDays, X509Certificate parentCertificate, PrivateKey parentPrivateKey) throws GeneralSecurityException {
        try {
            LangUtil.checkArg(validityDays > 0, () -> "Validity days must be positive: " + validityDays);

            // Validate subject DN format
            try {
                new javax.security.auth.x500.X500Principal(subject);
            } catch (IllegalArgumentException e) {
                throw new GeneralSecurityException("Invalid subject DN format: " + subject, e);
            }

            // Check parent certificate validity
            try {
                parentCertificate.checkValidity();
            } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                throw new GeneralSecurityException("Parent certificate is not valid", e);
            }

            // Verify parent certificate can sign (has CA constraint)
            boolean[] keyUsage = parentCertificate.getKeyUsage();
            if (keyUsage != null && !keyUsage[5]) { // keyCertSign bit
                throw new GeneralSecurityException("Parent certificate cannot sign certificates");
            }

            X509CertificateBuilder builder = X509CertificateBuilder.getBuilder(false)
                    .orElseThrow(() -> new GeneralSecurityException("No X.509 certificate builder available"));

            return builder
                    .subject(subject)
                    .issuer(parentCertificate.getSubjectX500Principal().getName())
                    .validityDays(validityDays)
                    .signatureAlgorithm(determineSignatureAlgorithm(parentPrivateKey))
                    .signedBy(parentCertificate, parentPrivateKey)
                    .addToChain(parentCertificate)
                    .build(keyPair);
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to create X.509 certificate", e);
        }
    }

    /**
     * Determines the appropriate signature algorithm based on the signing key's algorithm.
     * The signature algorithm must be compatible with the signing key that will create the signature.
     *
     * @param signingKey the private key that will be used to sign the certificate
     * @return the signature algorithm string (e.g., "SHA256withRSA", "SHA256withECDSA")
     * @throws GeneralSecurityException if the key algorithm is not supported or keys are incompatible
     */
    private static String determineSignatureAlgorithm(PrivateKey signingKey) throws GeneralSecurityException {
        String signingKeyAlgorithm = signingKey.getAlgorithm();

        try {
            AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(signingKeyAlgorithm.toUpperCase());
            return asymmAlg.getSignatureAlgorithm()
                    .orElseThrow(() -> new GeneralSecurityException("Algorithm " + asymmAlg + " does not support signing"));
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Unsupported signing key algorithm: " + signingKeyAlgorithm, e);
        }
    }
}
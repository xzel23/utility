package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
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
    public static X509Certificate[] createX509Certificate(KeyPair keyPair, String subject, int validityDays, X509Certificate parentCertificate, PrivateKey parentPrivateKey, boolean enableCA) throws GeneralSecurityException {
        try {
            LangUtil.checkArg(validityDays > 0, () -> "Validity days must be positive: " + validityDays);

            ensureDNSubjectIsValid(subject);
            ensureCertificateIsValid(parentCertificate);
            ensureCanSign(parentCertificate);

            X509CertificateBuilder builder = X509CertificateBuilder.getBuilder(enableCA)
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
     * Ensures that the provided X.509 certificate is valid by checking its validity dates.
     * If the certificate is expired or not yet valid, a {@link GeneralSecurityException} is thrown.
     *
     * @param parentCertificate the X.509 certificate to validate
     * @throws GeneralSecurityException if the certificate is not valid due to being expired or not yet valid
     */
    private static void ensureCertificateIsValid(X509Certificate parentCertificate) throws GeneralSecurityException {
        try {
            parentCertificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new GeneralSecurityException("certificate is not valid", e);
        }
    }

    /**
     * Ensures that the provided X.509 certificate has the ability to sign other certificates.
     * Verifies the "keyCertSign" bit in the certificate's key usage to determine if
     * it can be used for certificate signing. If the certificate lacks this capability,
     * a {@link GeneralSecurityException} is thrown.
     *
     * @param parentCertificate the X.509 certificate to be checked for signing capability.
     *                          This certificate must not be null and should represent
     *                          a valid certificate for verification.
     * @throws GeneralSecurityException if the certificate cannot be used to sign other certificates,
     *                                  as indicated by the "keyCertSign" bit in its key usage.
     */
    private static void ensureCanSign(X509Certificate parentCertificate) throws GeneralSecurityException {
        boolean[] keyUsage = parentCertificate.getKeyUsage();
        if (keyUsage != null && !keyUsage[5]) { // keyCertSign bit
            throw new GeneralSecurityException("certificate cannot sign certificates");
        }
    }

    /**
     * Validates whether the provided distinguished name (DN) subject string is in a valid
     * format for use in X.509 certificates. If the subject is invalid, a
     * {@link GeneralSecurityException} is thrown.
     *
     * @param subject the distinguished name (DN) of the certificate's subject, formatted as
     *                a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @throws GeneralSecurityException if the subject DN format is invalid
     */
    private static void ensureDNSubjectIsValid(String subject) throws GeneralSecurityException {
        try {
            new javax.security.auth.x500.X500Principal(subject);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("invalid subject DN format: " + subject, e);
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

    /**
     * Converts a byte array representing an encoded X.509 certificate into an
     * {@link X509Certificate} object.
     *
     * @param bytes the byte array containing the X.509 certificate data in DER-encoded format
     * @return an {@link X509Certificate} object representing the parsed certificate
     * @throws GeneralSecurityException if an error occurs while parsing the certificate,
     *                                  such as if the byte array does not represent
     *                                  a valid X.509 certificate
     */
    public static X509Certificate toX509Certificate(byte[] bytes) throws GeneralSecurityException {
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return readX509Certificate(in);
        } catch (IOException e) {
            // this should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads and parses an X.509 certificate from a PEM-encoded string.
     *
     * @param pemData the PEM-encoded string containing the X.509 certificate data
     * @return an {@link X509Certificate} object representing the parsed certificate
     * @throws GeneralSecurityException if an error occurs while parsing the certificate
     *                                  or if the provided data is not a valid X.509 certificate
     */
    public static X509Certificate toX509Certificate(String pemData) throws GeneralSecurityException {
        try (InputStream in = IoUtil.stringInputStream(pemData)) {
            return readX509Certificate(in);
        } catch (IOException e) {
            // this should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads and parses an X.509 certificate from the provided input stream.
     *
     * @param in the input stream containing the DER-encoded X.509 certificate data
     * @return an {@link X509Certificate} object representing the parsed certificate
     * @throws GeneralSecurityException if an error occurs while parsing the certificate
     *                                  or if the provided data is not a valid X.509 certificate
     */
    public static X509Certificate readX509Certificate(InputStream in) throws GeneralSecurityException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(in);
    }

}
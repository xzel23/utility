package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for certificate operations.
 */
public final class CertificateUtil {
    private static final Logger LOG = LogManager.getLogger(CertificateUtil.class);

    private static final String CERT_TYPE_X_509 = "X.509";

    /**
     * Utility class private constructor.
     */
    private CertificateUtil() { /* utility class */ }

    /**
     * Creates a self-signed X.509 certificate using the provided key pair, subject distinguished name,
     * validity duration, and a flag to enable Certificate Authority (CA) settings.
     *
     * @param keyPair      the key pair containing the private key used to sign the certificate
     *                     and the public key embedded within the certificate
     * @param subject      the distinguished name (DN) of the certificate's subject, formatted as
     *                     a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @param validityDays the number of days from the current date for which the certificate
     *                     will be valid
     * @param enableCA     a boolean flag indicating whether the certificate should include settings
     *                     for acting as a Certificate Authority (CA). If {@code true}, the certificate
     *                     will be configured as a CA.
     * @return an array of X.509 certificates, with the first certificate being the newly created
     * self-signed certificate. The array may include additional certificates if the chain
     * requires it.
     * @throws GeneralSecurityException if an error occurs during the certificate generation
     *                                  or signing process
     */
    public static X509Certificate[] createSelfSignedX509Certificate(KeyPair keyPair, String subject, int validityDays, boolean enableCA) throws GeneralSecurityException {
        Optional<X509CertificateBuilder> builder = X509CertificateBuilder.getBuilder(enableCA);
        if (builder.isEmpty()) {
            throw new GeneralSecurityException("No X.509 certificate builder available");
        }

        return builder.get()
                .subject(subject)
                .validityDays(validityDays)
                .signatureAlgorithm(determineSignatureAlgorithm(keyPair.getPrivate()))
                .build(keyPair);
    }

    /**
     * Creates a self-signed X.509 certificate generating the key pair from the provided key type and size.
     *
     * @param algorithm    the algorithm to use
     * @param keySize      the key size in bits (RSA/DSA: >=2048, EC: 256/384/521)
     * @param subject      the distinguished name (DN) of the certificate's subject
     * @param validityDays the number of days the certificate will be valid
     * @param enableCA     whether to include CA extensions
     * @return an array containing the generated self-signed certificate (and optional chain elements)
     * @throws GeneralSecurityException if key generation or certificate creation fails
     */
    public static X509Certificate[] createSelfSignedX509Certificate(AsymmetricAlgorithm algorithm, int keySize, String subject, int validityDays, boolean enableCA) throws GeneralSecurityException {
        KeyPair keyPair = KeyUtil.generateKeyPair(algorithm, keySize);
        return createSelfSignedX509Certificate(keyPair, subject, validityDays, enableCA);
    }

    /**
     * Creates an X.509 certificate using the provided key pair, subject distinguished name,
     * validity period, and a parent certificate. The generated certificate is included in a chain
     * along with the specified parent certificate(s).
     *
     * @param keyPair                the key pair containing the private key used to sign the certificate
     *                               and the public key embedded within the certificate
     * @param subject                the distinguished name (DN) of the certificate's subject, formatted as
     *                               a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country")
     * @param validityDays           the number of days from the current date for which the certificate
     *                               will be valid
     * @param enableCA               a boolean flag indicating whether the certificate should include settings
     *                               for acting as a Certificate Authority (CA). If {@code true}, the certificate
     *                               will be configured as a CA.
     * @param parentPrivateKey       the parent certificate's private key
     * @param parentCertificateChain the parent X.509 certificate to include in the generated certificate's
     *                               chain, establishing a chain of trust
     * @return an array of X.509 certificates, with the first certificate being the newly created
     * certificate, followed by the specified parent certificate(s) in the chain
     * @throws GeneralSecurityException if an error occurs during the certificate generation
     *                                  or signing process
     */
    public static X509Certificate[] createX509Certificate(
            KeyPair keyPair,
            String subject,
            int validityDays,
            boolean enableCA,
            PrivateKey parentPrivateKey,
            X509Certificate... parentCertificateChain
    ) throws GeneralSecurityException {
        try {
            LangUtil.checkArg(validityDays > 0, () -> "Validity days must be positive: " + validityDays);

            if (parentCertificateChain.length == 0) {
                throw new IllegalArgumentException("parent certificate chain must not be empty");
            }

            ensureDNSubjectIsValid(subject);
            ensureCertificateIsValid(parentCertificateChain[0]);
            ensureCanSign(parentCertificateChain[0]);

            X509CertificateBuilder builder = X509CertificateBuilder.getBuilder(enableCA)
                    .orElseThrow(() -> new GeneralSecurityException("No X.509 certificate builder available"));

            return builder
                    .subject(subject)
                    .issuer(parentCertificateChain[0].getSubjectX500Principal().getName())
                    .validityDays(validityDays)
                    .signatureAlgorithm(determineSignatureAlgorithm(parentPrivateKey))
                    .signedBy(parentPrivateKey, parentCertificateChain)
                    .build(keyPair);
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to create X.509 certificate", e);
        }
    }

    /**
     * Creates an X.509 certificate generating the subject key pair from the provided key type and size.
     *
     * @param algorithm    the algorithm to use
     * @param keySize                the key size in bits (RSA/DSA: >=2048, EC: 256/384/521)
     * @param subject                the distinguished name (DN) of the certificate's subject
     * @param validityDays           the number of days from the current date for which the certificate will be valid
     * @param enableCA               whether to include CA extensions on the child certificate
     * @param parentPrivateKey       the parent certificate's private key
     * @param parentCertificateChain the parent certificate chain used for signing
     * @return the generated certificate chain with the new certificate at position 0
     * @throws GeneralSecurityException if key generation or certificate creation fails
     */
    public static X509Certificate[] createX509Certificate(
            AsymmetricAlgorithm algorithm,
            int keySize,
            String subject,
            int validityDays,
            boolean enableCA,
            PrivateKey parentPrivateKey,
            X509Certificate... parentCertificateChain
    ) throws GeneralSecurityException {
        KeyPair keyPair = KeyUtil.generateKeyPair(algorithm, keySize);
        return createX509Certificate(keyPair, subject, validityDays, enableCA, parentPrivateKey, parentCertificateChain);
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
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private static void ensureDNSubjectIsValid(String subject) throws GeneralSecurityException {
        try {
            new javax.security.auth.x500.X500Principal(subject);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("invalid subject DN format: " + subject, e);
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
            AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(signingKeyAlgorithm.toUpperCase(Locale.ROOT));
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
     * Reads and parses an X.509 certificate from the provided input stream.
     *
     * @param in the input stream containing the DER-encoded X.509 certificate data
     * @return an {@link X509Certificate} object representing the parsed certificate
     * @throws GeneralSecurityException if an error occurs while parsing the certificate
     *                                  or if the provided data is not a valid X.509 certificate
     */
    public static X509Certificate readX509Certificate(InputStream in) throws GeneralSecurityException {
        CertificateFactory certFactory = CertificateFactory.getInstance(CERT_TYPE_X_509);
        return (X509Certificate) certFactory.generateCertificate(in);
    }

    /**
     * Converts a PEM-encoded string containing one or more X.509 certificates into an array
     * of {@link X509Certificate} instances. This method processes each certificate enclosed
     * in "BEGIN CERTIFICATE" and "END CERTIFICATE" markers within the PEM data, parses them,
     * and validates the resulting certificate chain.
     *
     * @param pemData the PEM-encoded string containing one or more X.509 certificates to be processed.
     *                Each certificate should be enclosed between "-----BEGIN CERTIFICATE-----"
     *                and "-----END CERTIFICATE-----".
     * @return an array of {@link X509Certificate} objects representing the certificate chain,
     * ordered from the leaf certificate to the root certificate.
     * @throws GeneralSecurityException if there is an issue parsing the certificates,
     *                                  validating the certificate chain, or if the input PEM data
     *                                  is invalid or improperly formatted.
     */
    public static X509Certificate[] toX509CertificateChain(String pemData) throws GeneralSecurityException {
        List<X509Certificate> certificates = new ArrayList<>();
        String[] lines = TextUtil.lines(pemData);
        StringBuilder currentCertPem = new StringBuilder();
        boolean inCertificate = false;

        for (String line : lines) {
            if (line.contains("-----BEGIN CERTIFICATE-----")) {
                inCertificate = true;
                currentCertPem = new StringBuilder();
                currentCertPem.append(line).append("\n");
            } else if (line.contains("-----END CERTIFICATE-----")) {
                if (!inCertificate) {
                    throw new IllegalStateException("Found end marker without start marker");
                }
                currentCertPem.append(line).append("\n");
                certificates.add(toX509Certificate(currentCertPem.toString()));
                inCertificate = false;
            } else if (inCertificate) {
                currentCertPem.append(line).append("\n");
            }
        }

        if (inCertificate) {
            throw new IllegalStateException("Certificate data ended without end marker");
        }

        X509Certificate[] chain = certificates.toArray(X509Certificate[]::new);

        verifyCertificateChain(chain);

        return chain;
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
     * Verifies the validity of a given X.509 certificate chain. For each certificate in the chain,
     * this method ensures that it is signed by the subsequent certificate in the chain.
     *
     * @param <T>          the generic certificate type
     * @param certificates an array of {@link X509Certificate} objects representing the certificate chain.
     *                     The first certificate in the array should be the leaf certificate, followed
     *                     by the intermediate certificates, and the last certificate should be the
     *                     root certificate.
     * @throws CertificateException if the chain is invalid, such as when a certificate is not
     *                              signed by the next certificate in the chain or has other
     *                              verification errors.
     */
    @SafeVarargs
    public static <T extends Certificate> void verifyCertificateChain(T... certificates) throws CertificateException {
        // verify that the certificate chain is valid
        for (int i = 0; i < certificates.length - 1; i++) {
            T currentCert = certificates[i];
            T parentCert = certificates[i + 1];

            X500Principal issuerDN =
                    ((X509Certificate) currentCert).getIssuerX500Principal();
            X500Principal subjectDN =
                    ((X509Certificate) parentCert).getSubjectX500Principal();
            if (!Objects.equals(issuerDN, subjectDN)) {
                throw new CertificateException(
                        "issuer of certificate %s ('%s') does not match parent certificate subject '%S'".formatted(
                                ((X509Certificate) currentCert).getSubjectX500Principal().toString(),
                                ((X509Certificate) currentCert).getIssuerX500Principal().toString(),
                                ((X509Certificate) parentCert).getSubjectX500Principal().toString()
                        )
                );
            }

            try {
                currentCert.verify(parentCert.getPublicKey());
            } catch (InvalidKeyException e) {
                throw new CertificateException(currentCert + " is not signed by the public key of " + parentCert, e);
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new CertificateException(currentCert + " could not be verified", e);
            } catch (SignatureException e) {
                throw new CertificateException("the signature of " + currentCert + " is invalid", e);
            }
        }
    }

    /**
     * Converts one or more certificates into their PEM-encoded string representation.
     * <p>
     * <strong>Note:</strong> Most tools only support X509 certificates
     *
     * @param <T>          the generic certificate type
     * @param certificates one or more X509 certificates to be converted to PEM format
     * @return a string containing the PEM-encoded representation of the provided certificates
     * @throws CertificateEncodingException if an encoding error occurs while converting the certificates
     * @throws UncheckedIOException         if an unexpected I/O exception happens during the operation
     */
    @SafeVarargs
    public static <T extends Certificate> String toPem(T... certificates) throws CertificateEncodingException {
        StringBuilder sb = new StringBuilder(certificates.length * 1600);
        try {
            return writePem(sb, certificates).toString();
        } catch (IOException e) {
            // this should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes a PEM-encoded representation of a chain of certificates to the provided writer.
     * Each certificate in the chain is written enclosed between "-----BEGIN CERTIFICATE-----"
     * and "-----END CERTIFICATE-----" markers.
     * <p>
     * <strong>Note:</strong> Most tools only support X509 certificates
     *
     * @param <T>          the generic certificate type*
     * @param <U>          the generic type of the {@link Appendable} used for output
     * @param app          the {@link Writer} used to write the PEM-encoded certificates.
     *                     The writer must be initialized prior to calling this method, and the caller
     *                     is responsible for closing the writer after use.
     * @param certificates a varargs array of {@link X509Certificate} objects representing the certificate
     *                     chain to be written. Each certificate in the array is encoded and written
     *                     in the order specified.
     * @return the appendable used for output
     * @throws IOException                  if an I/O error occurs while writing to the writer.
     * @throws CertificateEncodingException if an error occurs while encoding a certificate to the
     *                                      DER format for PEM conversion.
     */
    @SafeVarargs
    public static <T extends Certificate, U extends Appendable> U writePem(U app, T... certificates) throws IOException, CertificateEncodingException {
        for (T certificate : certificates) {
            if (!(certificate instanceof X509Certificate)) {
                LOG.warn("exporting certificate of non-X509 type {} as PEM", certificate.getType());
            }

            app.append("-----BEGIN CERTIFICATE-----\n")
                    .append(java.util.Base64.getMimeEncoder().encodeToString(certificate.getEncoded()))
                    .append("\n-----END CERTIFICATE-----\n");
        }
        return app;
    }

    /**
     * Converts an array of certificates into a byte array in PKCS#7 format.
     *
     * @param certificates an array of certificates to be converted
     * @return a byte array containing the certificates encoded in PKCS#7 format
     * @throws CertificateException if an error occurs during certificate processing
     */
    public static byte[] toPkcs7Bytes(Certificate... certificates) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE_X_509);
        List<Certificate> certList = Arrays.asList(certificates);
        CertPath certPath = cf.generateCertPath(certList);
        return certPath.getEncoded("PKCS7");
    }

    /**
     * Converts a byte array representing certificates into an array of Certificate objects.
     *
     * @param pkcs7Bytes the byte array containing the encoded certificates
     * @return an array of Certificate objects generated from the provided byte array
     * @throws CertificateException if an error occurs while creating the certificates
     */
    public static Certificate[] pkcs7BytesToCertificateChain(byte[] pkcs7Bytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE_X_509);
        CertPath certPath = cf.generateCertPath(new ByteArrayInputStream(pkcs7Bytes), "PKCS7");
        return certPath.getCertificates().toArray(Certificate[]::new);
    }

    /**
     * Checks if the given X509 certificate is self-signed.
     * <p>
     * A certificate is considered self-signed if its issuer and subject are the same
     * and its signature can be verified with its own public key.
     *
     * @param cert the X509 certificate to check
     * @return true if the certificate is self-signed, false otherwise
     * @throws GeneralSecurityException if an error occurs during the signature verification process
     */
    public static boolean isSelfSigned(X509Certificate cert) throws GeneralSecurityException {
        try {
            // Check if issuer and subject are the same
            if (!cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
                return false;
            }

            // Verify the signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException | InvalidKeyException e) {
            // Signature does not verify with its own public key
            return false;
        }
    }
}
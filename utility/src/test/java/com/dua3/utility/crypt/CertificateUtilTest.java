// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CertificateUtilTest {

    private static final Logger LOG = LogManager.getLogger(CertificateUtilTest.class);

    @Test
    void testCreateSelfSignedX509Certificate() throws Exception {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test Subject, O=Test Organization, C=US";
        int validityDays = 365;

        // Create a self-signed certificate
        Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays, true);

        // Verify the certificate was created
        assertNotNull(certificates);
        assertTrue(certificates.length > 0);
        assertNotNull(certificates[0]);

        // Verify the certificate contains the expected public key
        PublicKey certPublicKey = certificates[0].getPublicKey();
        assertNotNull(certPublicKey);
        assertArrayEquals(keyPair.getPublic().getEncoded(), certPublicKey.getEncoded());

        // Verify the certificate can be verified with its own public key
        try {
            certificates[0].verify(keyPair.getPublic());
        } catch (Exception e) {
            fail("Certificate verification failed: " + e.getMessage());
        }

        // Verify the certificate's string representation contains the subject
        for (String part : subject.split("[,\n]")) {
            String strippedPart = part.strip();
            assertTrue(certificates[0].toString().contains(strippedPart), "Certificate string should contain '" + strippedPart + "'");
        }
    }

    @Test
    void testCreateX509Certificate() throws Exception {
        // Generate a key pair for the certificate to be tested
        KeyPair childKeyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Child, O=Test Organization, C=US";
        int validityDays = 180;

        // Create a parent certificate
        Map<String, Object> parentInfo = createTestParentCertificate();
        X509Certificate parentCertificate = (X509Certificate) parentInfo.get("certificate");
        PrivateKey parentPrivateKey = (PrivateKey) parentInfo.get("privateKey");

        // Create a certificate signed by the parent
        X509Certificate[] certificates = CertificateUtil.createX509Certificate(
                childKeyPair, subject, validityDays, false, parentPrivateKey, parentCertificate);

        // Verify the certificate was created
        assertNotNull(certificates);
        assertTrue(certificates.length > 0);
        assertNotNull(certificates[0]);

        // Verify the certificate contains the expected public key
        PublicKey certPublicKey = certificates[0].getPublicKey();
        assertNotNull(certPublicKey);
        assertArrayEquals(childKeyPair.getPublic().getEncoded(), certPublicKey.getEncoded());

        // Verify the certificate was signed by the parent
        try {
            certificates[0].verify(parentCertificate.getPublicKey());
        } catch (Exception e) {
            fail("Certificate verification with parent certificate failed: " + e.getMessage());
        }

        // Verify the certificate's string representation contains the subject
        for (String part : subject.split("[,\n]")) {
            String strippedPart = part.strip();
            assertTrue(certificates[0].toString().contains(strippedPart),
                    "Certificate string should contain '" + strippedPart + "'");
        }

        // verify that the certificate chain is valid
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(certificates));

        // Verify that both certificates have different public keys
        assertNotEquals(certificates[1].getPublicKey(), certificates[0].getPublicKey());
    }

    /**
     * Helper method to create a self-signed parent certificate for testing.
     */
    private Map<String, Object> createTestParentCertificate() throws Exception {
        KeyPair parentKeyPair = KeyUtil.generateRSAKeyPair();
        // Use the format that will be used as issuer in the child certificate
        String parentSubject = "C=US, O=Test Organization, CN=Parent";
        int parentValidityDays = 365;

        X509Certificate[] parentCertificates = CertificateUtil.createSelfSignedX509Certificate(
                parentKeyPair, parentSubject, parentValidityDays, true);

        // Print the actual subject DN of the created certificate
        System.out.println("[DEBUG_LOG] Parent certificate created with subject: " + parentSubject);
        System.out.println("[DEBUG_LOG] Actual parent certificate subject: " +
                parentCertificates[0].getSubjectX500Principal().toString());
        System.out.println("[DEBUG_LOG] Parent certificate subject (RFC2253): " +
                parentCertificates[0].getSubjectX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253));

        return Map.of(
                "certificate", parentCertificates[0],
                "privateKey", parentKeyPair.getPrivate()
        );
    }

    @Test
    void testToX509Certificate() throws Exception {
        // Generate a self-signed certificate as bytes
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test To X509 Certificate, O=Test Organization, C=US";
        int validityDays = 365;

        Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays, true);
        X509Certificate originalCertificate = (X509Certificate) certificates[0];
        byte[] certBytes = originalCertificate.getEncoded();

        // Test conversion of bytes back to a certificate
        X509Certificate convertedCertificate = CertificateUtil.toX509Certificate(certBytes);
        assertNotNull(convertedCertificate, "The converted certificate should not be null.");
        assertArrayEquals(originalCertificate.getEncoded(), convertedCertificate.getEncoded(), "The certificate bytes should match.");
        assertTrue(convertedCertificate.toString().contains("CN=Test To X509 Certificate"), "The certificate subject should match the original.");

        // Test with invalid data
        byte[] invalidBytes = "Invalid Bytes".getBytes(StandardCharsets.UTF_8);
        assertThrows(CertificateException.class, () -> CertificateUtil.toX509Certificate(invalidBytes));
    }

    @Test
    void testToX509CertificateFromPemString() throws Exception {
        // Create a self-signed certificate
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test PEM Certificate, O=Test Organization, C=US";
        int validityDays = 365;

        Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays, true);
        X509Certificate originalCertificate = (X509Certificate) certificates[0];

        // Convert the certificate to PEM (assuming utility to create PEM string)
        String pemData = "-----BEGIN CERTIFICATE-----\n"
                + java.util.Base64.getEncoder().encodeToString(originalCertificate.getEncoded())
                + "\n-----END CERTIFICATE-----";

        // Test conversion
        X509Certificate convertedCertificate = PemData.parse(pemData).asCertificate();
        assertNotNull(convertedCertificate, "The converted certificate should not be null.");
        assertArrayEquals(originalCertificate.getEncoded(), convertedCertificate.getEncoded(), "The certificate bytes should match.");
        assertTrue(convertedCertificate.toString().contains("CN=Test PEM Certificate"), "The certificate subject should match the original.");

        // Test with invalid PEM data
        String invalidPem = "-----BEGIN CERTIFICATE-----\nInvalid PEM Data\n-----END CERTIFICATE-----";
        assertThrows(IllegalStateException.class, () -> PemData.parse(invalidPem).asCertificate());
    }

    @Test
    void testCreateX509CertificateWithParentChain() throws Exception {
        // Generate a key pair for the certificate to be tested
        KeyPair childKeyPair = KeyUtil.generateRSAKeyPair();
        String childSubject = "CN=Child, O=Test Organization, C=US";
        int childValidityDays = 180;

        // Create a root certificate
        KeyPair rootKeyPair = KeyUtil.generateRSAKeyPair();
        String rootSubject = "CN=Root, O=Test Organization, C=US";
        int rootValidityDays = 730;
        X509Certificate[] rootCertificates = CertificateUtil.createSelfSignedX509Certificate(
                rootKeyPair, rootSubject, rootValidityDays, true);
        X509Certificate rootCertificate = rootCertificates[0];

        // Create an intermediate certificate signed by the root
        KeyPair intermediateKeyPair = KeyUtil.generateRSAKeyPair();
        String intermediateSubject = "CN=Intermediate, O=Test Organization, C=US";
        int intermediateValidityDays = 365;
        X509Certificate[] intermediateCertificates = CertificateUtil.createX509Certificate(
                intermediateKeyPair, intermediateSubject, intermediateValidityDays, true,
                rootKeyPair.getPrivate(), rootCertificate);
        X509Certificate intermediateCertificate = intermediateCertificates[0];

        // Create a second intermediate certificate signed by the first intermediate
        KeyPair intermediate2KeyPair = KeyUtil.generateRSAKeyPair();
        String intermediate2Subject = "CN=Intermediate2, O=Test Organization, C=US";
        int intermediate2ValidityDays = 365;
        X509Certificate[] intermediate2Certificates = CertificateUtil.createX509Certificate(
                intermediate2KeyPair, intermediate2Subject, intermediate2ValidityDays, true,
                intermediateKeyPair.getPrivate(), intermediateCertificate, rootCertificate);
        X509Certificate intermediate2Certificate = intermediate2Certificates[0];

        // Create a certificate signed by the second intermediate with a chain of three certificates
        X509Certificate[] certificates = CertificateUtil.createX509Certificate(
                childKeyPair, childSubject, childValidityDays, false,
                intermediate2KeyPair.getPrivate(),
                intermediate2Certificate, intermediateCertificate, rootCertificate);

        // Verify the certificate was created
        assertNotNull(certificates);
        assertTrue(certificates.length > 0);
        assertNotNull(certificates[0]);

        // Verify the certificate contains the expected public key
        PublicKey certPublicKey = certificates[0].getPublicKey();
        assertNotNull(certPublicKey);
        assertArrayEquals(childKeyPair.getPublic().getEncoded(), certPublicKey.getEncoded());

        // Verify the certificate was signed by the immediate parent (intermediate2)
        try {
            certificates[0].verify(intermediate2Certificate.getPublicKey());
        } catch (Exception e) {
            fail("Certificate verification with immediate parent certificate failed: " + e.getMessage());
        }

        // Verify the certificate's string representation contains the subject
        for (String part : childSubject.split("[,\n]")) {
            String strippedPart = part.strip();
            assertTrue(certificates[0].toString().contains(strippedPart),
                    "Certificate string should contain '" + strippedPart + "'");
        }

        // Verify that the certificate chain is valid and complete
        // The chain should contain 4 certificates: child, intermediate2, intermediate, root
        assertEquals(4, certificates.length, "Certificate chain should contain 4 certificates");
        assertEquals(rootCertificate, certificates[certificates.length - 1], "root should be last entry.");
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(certificates));

        // Verify that both certificates have different public keys
        assertNotEquals(certificates[1].getPublicKey(), certificates[0].getPublicKey());
    }

    @Test
    void testToX509CertificateChain() throws Exception {
        // Create a root certificate
        KeyPair rootKeyPair = KeyUtil.generateRSAKeyPair();
        String rootSubject = "CN=Root, O=Test Organization, C=US";
        int rootValidityDays = 730;
        X509Certificate[] rootCertificates = CertificateUtil.createSelfSignedX509Certificate(
                rootKeyPair, rootSubject, rootValidityDays, true);
        X509Certificate rootCertificate = rootCertificates[0];

        // Create an intermediate certificate signed by the root
        KeyPair intermediateKeyPair = KeyUtil.generateRSAKeyPair();
        String intermediateSubject = "CN=Intermediate, O=Test Organization, C=US";
        int intermediateValidityDays = 365;
        X509Certificate[] intermediateCertificates = CertificateUtil.createX509Certificate(
                intermediateKeyPair, intermediateSubject, intermediateValidityDays, true,
                rootKeyPair.getPrivate(), rootCertificate);
        X509Certificate intermediateCertificate = intermediateCertificates[0];

        // Convert certificates to PEM format and concatenate
        String rootPem = "-----BEGIN CERTIFICATE-----\n"
                + java.util.Base64.getEncoder().encodeToString(rootCertificate.getEncoded())
                + "\n-----END CERTIFICATE-----\n";
        String intermediatePem = "-----BEGIN CERTIFICATE-----\n"
                + java.util.Base64.getEncoder().encodeToString(intermediateCertificate.getEncoded())
                + "\n-----END CERTIFICATE-----";
        String concatenatedPem = intermediatePem + "\n" + rootPem;

        // Convert PEM string back to certificate chain and validate
        X509Certificate[] chain = PemData.parse(concatenatedPem).asCertificateChainArray();
        assertNotNull(chain, "The certificate chain should not be null.");
        assertEquals(2, chain.length, "The certificate chain should contain 2 certificates.");
        assertEquals(intermediateCertificate, chain[0], "The first certificate in the chain should be the intermediate.");
        assertEquals(rootCertificate, chain[1], "The second certificate in the chain should be the root.");
    }

    @Test
    void testToX509CertificateChainWithInvalidData() {
        // Invalid PEM data with missing end marker
        String invalidPem = "-----BEGIN CERTIFICATE-----\nInvalid Data";
        assertThrows(PemData.PemException.class, () -> PemData.parse(invalidPem).asCertificateChainArray(),
                "An exception should be thrown for invalid PEM data.");
    }

    @Test
    void testToX509CertificateChainWithEmptyData() throws Exception {
        // Invalid PEM data does not contain certificates
        String emptyPem = "";
        assertEquals(0, PemData.parse(emptyPem).asCertificateChainArray().length, "Chain should have 0 length for empty PEM data.");
    }

    @Test
    void testCreateSelfSignedCertificate_InvalidSubject() throws InvalidAlgorithmParameterException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String invalidSubject = "Invalid Subject Format";
        int validityDays = 365;

        // Verify that creating a certificate with an invalid subject throws an exception
        assertThrows(
                IllegalArgumentException.class,
                () -> CertificateUtil.createSelfSignedX509Certificate(keyPair, invalidSubject, validityDays, true)
        );
    }

    @Test
    void testCreateSelfSignedCertificate_InvalidValidityDays() throws InvalidAlgorithmParameterException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Invalid Validity Days, O=Test Organization, C=US";

        // Test with zero and negative validity days
        assertThrows(IllegalArgumentException.class,
                () -> CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, 0, true),
                "Expected exception for zero validity days.");
        assertThrows(IllegalArgumentException.class,
                () -> CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, -1, true),
                "Expected exception for negative validity days.");
    }

    @Test
    void testWritePem() throws Exception {
        // Create a test certificate
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test WritePem, O=Test Organization, C=US";
        int validityDays = 365;
        X509Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(
                keyPair, subject, validityDays, true);
        X509Certificate certificate = certificates[0];

        // Test writing a single certificate
        StringBuilder sb = new StringBuilder();
        CertificateUtil.writePem(sb, certificate);
        String pemOutput = sb.toString();

        // Verify the PEM format is correct
        assertTrue(TextUtil.toUnixLineEnds(pemOutput).startsWith("-----BEGIN CERTIFICATE-----\n"), "PEM should start with BEGIN marker");
        assertTrue(TextUtil.toUnixLineEnds(pemOutput).endsWith("-----END CERTIFICATE-----\n"), "PEM should end with END marker");
        assertTrue(containsIgnoringLines(pemOutput, java.util.Base64.getMimeEncoder().encodeToString(certificate.getEncoded())),
                "PEM should contain Base64 encoded certificate");

        // Test writing multiple certificates
        KeyPair keyPair2 = KeyUtil.generateRSAKeyPair();
        String subject2 = "CN=Test WritePem 2, O=Test Organization, C=US";
        X509Certificate[] certificates2 = CertificateUtil.createSelfSignedX509Certificate(
                keyPair2, subject2, validityDays, true);
        X509Certificate certificate2 = certificates2[0];

        StringBuilder sb2 = new StringBuilder();
        CertificateUtil.writePem(sb2, certificate, certificate2);
        String pemOutput2 = sb2.toString();

        // Verify both certificates are in the output
        assertTrue(containsIgnoringLines(pemOutput2, java.util.Base64.getMimeEncoder().encodeToString(certificate.getEncoded())),
                "PEM should contain first certificate");
        assertTrue(containsIgnoringLines(pemOutput2, java.util.Base64.getMimeEncoder().encodeToString(certificate2.getEncoded())),
                "PEM should contain second certificate");
        assertEquals(2, pemOutput2.split("-----BEGIN CERTIFICATE-----").length - 1,
                "PEM should contain two BEGIN markers");
        assertEquals(2, pemOutput2.split("-----END CERTIFICATE-----").length - 1,
                "PEM should contain two END markers");

        // Test with empty array
        StringBuilder sb3 = new StringBuilder();
        CertificateUtil.writePem(sb3);
        assertEquals("", sb3.toString(), "PEM output should be empty for empty array");
    }

    /**
     * Joins multiple lines of the given string into a single line by removing all line break characters.
     * <p>
     * This is needed when comparing PEM strings since standard BASE64 uses 76 characters per line
     * whereas for PEM files often use 64 characters are used.
     *
     * @param pemOutput the input string containing line breaks
     * @return a single-line string with all line breaks removed
     */
    private static String joinLines(String pemOutput) {
        return pemOutput.replaceAll("\\R", "");
    }

    @Test
    void testToPem() throws Exception {
        // Create a test certificate
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test ToPem, O=Test Organization, C=US";
        int validityDays = 365;
        X509Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(
                keyPair, subject, validityDays, true);
        X509Certificate certificate = certificates[0];

        // Test converting a single certificate
        String pemOutput = CertificateUtil.toPem(certificate);

        // Verify the PEM format is correct
        assertTrue(TextUtil.toUnixLineEnds(pemOutput).startsWith("-----BEGIN CERTIFICATE-----\n"), "PEM should start with BEGIN marker");
        assertTrue(TextUtil.toUnixLineEnds(pemOutput).endsWith("-----END CERTIFICATE-----\n"), "PEM should end with END marker");
        assertTrue(containsIgnoringLines(pemOutput, java.util.Base64.getMimeEncoder().encodeToString(certificate.getEncoded())),
                "PEM should contain Base64 encoded certificate");

        // Test converting multiple certificates
        KeyPair keyPair2 = KeyUtil.generateRSAKeyPair();
        String subject2 = "CN=Test ToPem 2, O=Test Organization, C=US";
        X509Certificate[] certificates2 = CertificateUtil.createSelfSignedX509Certificate(
                keyPair2, subject2, validityDays, true);
        X509Certificate certificate2 = certificates2[0];

        String pemOutput2 = CertificateUtil.toPem(certificate, certificate2);

        // Verify both certificates are in the output
        assertTrue(containsIgnoringLines(pemOutput2, java.util.Base64.getMimeEncoder().encodeToString(certificate.getEncoded())),
                "PEM should contain first certificate");
        assertTrue(containsIgnoringLines(pemOutput2, java.util.Base64.getMimeEncoder().encodeToString(certificate2.getEncoded())),
                "PEM should contain second certificate");
        assertEquals(2, pemOutput2.split("-----BEGIN CERTIFICATE-----").length - 1,
                "PEM should contain two BEGIN markers");
        assertEquals(2, pemOutput2.split("-----END CERTIFICATE-----").length - 1,
                "PEM should contain two END markers");

        // Test with empty array
        String pemOutput3 = CertificateUtil.toPem();
        assertEquals("", pemOutput3, "PEM output should be empty for empty array");
    }

    @Test
    void testRoundTripSingleCertificate() throws Exception {
        // Create a test certificate
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test Round Trip, O=Test Organization, C=US";
        int validityDays = 365;
        X509Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(
                keyPair, subject, validityDays, true);
        X509Certificate certificate = certificates[0];

        // Convert to PEM
        String pemOutput = CertificateUtil.toPem(certificate);

        // Convert back to certificate chain
        X509Certificate[] roundTripCertificates = PemData.parse(pemOutput).asCertificateChainArray();

        // Verify the round trip
        assertEquals(1, roundTripCertificates.length, "Should have one certificate after round trip");
        assertArrayEquals(certificate.getEncoded(), roundTripCertificates[0].getEncoded(),
                "Certificate should be the same after round trip");

        // Convert back to PEM
        String finalPemOutput = CertificateUtil.toPem(roundTripCertificates);

        // Verify the final PEM matches the original
        assertEquals(pemOutput, finalPemOutput, "Final PEM should match original PEM");
    }

    @Test
    void testRoundTripCertificateChain() throws Exception {
        // Create a root certificate
        KeyPair rootKeyPair = KeyUtil.generateRSAKeyPair();
        String rootSubject = "CN=Root Round Trip, O=Test Organization, C=US";
        int rootValidityDays = 730;
        X509Certificate[] rootCertificates = CertificateUtil.createSelfSignedX509Certificate(
                rootKeyPair, rootSubject, rootValidityDays, true);
        X509Certificate rootCertificate = rootCertificates[0];

        // Create an intermediate certificate signed by the root
        KeyPair intermediateKeyPair = KeyUtil.generateRSAKeyPair();
        String intermediateSubject = "CN=Intermediate Round Trip, O=Test Organization, C=US";
        int intermediateValidityDays = 365;
        X509Certificate[] intermediateCertificates = CertificateUtil.createX509Certificate(
                intermediateKeyPair, intermediateSubject, intermediateValidityDays, true,
                rootKeyPair.getPrivate(), rootCertificate);
        X509Certificate intermediateCertificate = intermediateCertificates[0];

        // Create the chain
        X509Certificate[] originalChain = {intermediateCertificate, rootCertificate};

        // Convert to PEM
        String pemOutput = CertificateUtil.toPem(originalChain);

        // Convert back to certificate chain
        X509Certificate[] roundTripChain = PemData.parse(pemOutput).asCertificateChainArray();

        // Verify the round trip
        assertEquals(originalChain.length, roundTripChain.length,
                "Chain length should be the same after round trip");
        for (int i = 0; i < originalChain.length; i++) {
            assertArrayEquals(originalChain[i].getEncoded(), roundTripChain[i].getEncoded(),
                    "Certificate at index " + i + " should be the same after round trip");
        }

        // Convert back to PEM
        String finalPemOutput = CertificateUtil.toPem(roundTripChain);

        // Verify the final PEM matches the original
        assertEquals(pemOutput, finalPemOutput, "Final PEM should match original PEM");
    }

    @Test
    void testPkcs7RoundTripSingleCertificate() throws Exception {
        // Create a self-signed certificate
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=PKCS7 Single, O=Test Organization, C=US";
        X509Certificate[] chain = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, 365, true);
        X509Certificate cert = chain[0];

        // Convert to PKCS#7 bytes
        byte[] pkcs7 = CertificateUtil.toPkcs7Bytes(cert);
        assertNotNull(pkcs7, "PKCS#7 bytes should not be null");
        assertTrue(pkcs7.length > 0, "PKCS#7 bytes should not be empty");

        // Convert back to certificate chain
        Certificate[] parsed = CertificateUtil.pkcs7BytesToCertificateChain(pkcs7);
        assertNotNull(parsed, "Parsed chain should not be null");
        assertEquals(1, parsed.length, "Parsed chain should contain one certificate");

        // Verify equality by comparing encoded forms
        assertArrayEquals(cert.getEncoded(), parsed[0].getEncoded(), "Certificate should match after PKCS#7 roundtrip");
    }

    @Test
    void testPkcs7RoundTripCertificateChain() throws Exception {
        // Create root (self-signed)
        KeyPair rootKeyPair = KeyUtil.generateRSAKeyPair();
        X509Certificate root = CertificateUtil.createSelfSignedX509Certificate(rootKeyPair, "CN=Root PKCS7, O=Test, C=US", 730, true)[0];

        // Create intermediate signed by root
        KeyPair interKeyPair = KeyUtil.generateRSAKeyPair();
        X509Certificate intermediate = CertificateUtil.createX509Certificate(
                interKeyPair,
                "CN=Intermediate PKCS7, O=Test, C=US",
                365,
                true,
                rootKeyPair.getPrivate(),
                root
        )[0];

        // Build chain (leaf first, then parent)
        Certificate[] chain = new Certificate[]{intermediate, root};

        // Convert to PKCS#7 bytes
        byte[] pkcs7 = CertificateUtil.toPkcs7Bytes(chain);
        assertNotNull(pkcs7, "PKCS#7 bytes should not be null");
        assertTrue(pkcs7.length > 0, "PKCS#7 bytes should not be empty");

        // Convert back
        Certificate[] parsed = CertificateUtil.pkcs7BytesToCertificateChain(pkcs7);
        assertNotNull(parsed, "Parsed chain should not be null");
        assertEquals(2, parsed.length, "Parsed chain should have two certificates");

        // Ensure both original certs are present (order-agnostic)
        int idxIntermediate = -1;
        int idxRoot = -1;
        for (int i = 0; i < parsed.length; i++) {
            if (java.util.Arrays.equals(intermediate.getEncoded(), parsed[i].getEncoded())) {
                idxIntermediate = i;
            } else if (java.util.Arrays.equals(root.getEncoded(), parsed[i].getEncoded())) {
                idxRoot = i;
            }
        }
        assertNotEquals(-1, idxIntermediate, "Intermediate certificate should be present after roundtrip");
        assertNotEquals(-1, idxRoot, "Root certificate should be present after roundtrip");

        // Build leaf-first chain for verification
        X509Certificate leafFirst0 = (X509Certificate) parsed[idxIntermediate];
        X509Certificate leafFirst1 = (X509Certificate) parsed[idxRoot];

        // Verify resulting chain is valid (leaf -> root)
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(leafFirst0, leafFirst1));
    }

    // New tests for keyType/keySize overloads

    @Test
    void testCreateSelfSignedX509Certificate_WithKeyTypeAndSize_RSA() throws Exception {
        String subject = "CN=Self RSA, O=Test Org, C=US";
        X509Certificate[] chain = CertificateUtil.createSelfSignedX509Certificate(AsymmetricAlgorithm.RSA, 2048, subject, 365, true);
        assertNotNull(chain);
        assertTrue(chain.length >= 1);
        X509Certificate cert = chain[0];
        assertEquals("RSA", cert.getPublicKey().getAlgorithm());
        assertTrue(cert.toString().contains("CN=Self RSA"));
        // self-verify
        assertDoesNotThrow(() -> cert.verify(cert.getPublicKey()));
    }

    @Test
    void testCreateSelfSignedX509Certificate_WithKeyTypeAndSize_EC() throws Exception {
        String subject = "CN=Self EC, O=Test Org, C=US";
        X509Certificate[] chain = CertificateUtil.createSelfSignedX509Certificate(AsymmetricAlgorithm.EC, 256, subject, 365, true);
        assertNotNull(chain);
        assertTrue(chain.length >= 1);
        X509Certificate cert = chain[0];
        assertEquals("EC", cert.getPublicKey().getAlgorithm());
        assertTrue(cert.toString().contains("CN=Self EC"));
        assertDoesNotThrow(() -> cert.verify(cert.getPublicKey()));
    }

    @Test
    void testCreateX509Certificate_WithKeyTypeAndSize_RSA_ChildSignedByRSAParent() throws Exception {
        // Parent (RSA)
        Map<String, Object> parentInfo = createTestParentCertificate();
        X509Certificate parentCert = (X509Certificate) parentInfo.get("certificate");
        PrivateKey parentPrivateKey = (PrivateKey) parentInfo.get("privateKey");

        // Child via new overload (RSA 2048)
        String subject = "CN=Child RSA, O=Test Org, C=US";
        X509Certificate[] chain = CertificateUtil.createX509Certificate(
                AsymmetricAlgorithm.RSA, 2048, subject, 180, false, parentPrivateKey, parentCert);

        assertNotNull(chain);
        assertTrue(chain.length >= 2, "Chain should contain child and parent");
        X509Certificate child = chain[0];
        assertEquals("RSA", child.getPublicKey().getAlgorithm());
        // verify signature with parent public key
        assertDoesNotThrow(() -> child.verify(parentCert.getPublicKey()));
        // verify chain
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(chain));
    }

    @Test
    void testCreateX509Certificate_WithKeyTypeAndSize_EC_ChildSignedByRSAParent() throws Exception {
        // Parent (RSA)
        Map<String, Object> parentInfo = createTestParentCertificate();
        X509Certificate parentCert = (X509Certificate) parentInfo.get("certificate");
        PrivateKey parentPrivateKey = (PrivateKey) parentInfo.get("privateKey");

        // Child via new overload (EC 256)
        String subject = "CN=Child EC, O=Test Org, C=US";
        X509Certificate[] chain = CertificateUtil.createX509Certificate(
                AsymmetricAlgorithm.EC, 256, subject, 180, false, parentPrivateKey, parentCert);

        assertNotNull(chain);
        assertTrue(chain.length >= 2, "Chain should contain child and parent");
        X509Certificate child = chain[0];
        assertEquals("EC", child.getPublicKey().getAlgorithm());
        // verify signature with parent public key
        assertDoesNotThrow(() -> child.verify(parentCert.getPublicKey()));
        // verify chain
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(chain));
    }

    @Test
    void testCertificateChainPropagation() throws Exception {
        // Create root certificate
        KeyPair rootKeyPair = KeyUtil.generateRSAKeyPair();
        String rootSubject = "CN=Root,O=Test Organization,C=US";
        X509Certificate[] rootChain = CertificateUtil.createSelfSignedX509Certificate(
                rootKeyPair, rootSubject, 730, true);

        assertEquals(1, rootChain.length, "Root chain should contain 1 certificate");
        LOG.debug("Root chain length: {}", rootChain.length);

        // Create certificate 1 signed by root
        KeyPair keyPair1 = KeyUtil.generateRSAKeyPair();
        String subject1 = "CN=Certificate-1,O=Test Organization,C=US";
        X509Certificate[] chain1 = CertificateUtil.createX509Certificate(
                keyPair1, subject1, 365, true,
                rootKeyPair.getPrivate(), rootChain);

        assertEquals(2, chain1.length, "Chain-1 should contain 2 certificates (cert-1 + root)");
        assertEquals("CN=Certificate-1,O=Test Organization,C=US", chain1[0].getSubjectX500Principal().getName());
        assertEquals("CN=Root,O=Test Organization,C=US", chain1[1].getSubjectX500Principal().getName());
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(chain1));
        LOG.debug("Chain-1 length: {}", chain1.length);

        // Create certificate 2 signed by certificate 1
        KeyPair keyPair2 = KeyUtil.generateRSAKeyPair();
        String subject2 = "CN=Certificate-2,O=Test Organization,C=US";
        X509Certificate[] chain2 = CertificateUtil.createX509Certificate(
                keyPair2, subject2, 365, true,
                keyPair1.getPrivate(), chain1);

        assertEquals(3, chain2.length, "Chain-2 should contain 3 certificates (cert-2 + cert-1 + root)");
        assertEquals("CN=Certificate-2,O=Test Organization,C=US", chain2[0].getSubjectX500Principal().getName());
        assertEquals("CN=Certificate-1,O=Test Organization,C=US", chain2[1].getSubjectX500Principal().getName());
        assertEquals("CN=Root,O=Test Organization,C=US", chain2[2].getSubjectX500Principal().getName());
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(chain2));
        LOG.debug("Chain-2 length: {}", chain2.length);

        // Create certificate 3 signed by certificate 2
        KeyPair keyPair3 = KeyUtil.generateRSAKeyPair();
        String subject3 = "CN=Certificate-3,O=Test Organization,C=US";
        X509Certificate[] chain3 = CertificateUtil.createX509Certificate(
                keyPair3, subject3, 365, false,
                keyPair2.getPrivate(), chain2);

        assertEquals(4, chain3.length, "Chain-3 should contain 4 certificates (cert-3 + cert-2 + cert-1 + root)");
        assertEquals("CN=Certificate-3,O=Test Organization,C=US", chain3[0].getSubjectX500Principal().getName());
        assertEquals("CN=Certificate-2,O=Test Organization,C=US", chain3[1].getSubjectX500Principal().getName());
        assertEquals("CN=Certificate-1,O=Test Organization,C=US", chain3[2].getSubjectX500Principal().getName());
        assertEquals("CN=Root,O=Test Organization,C=US", chain3[3].getSubjectX500Principal().getName());
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain(chain3));
        LOG.debug("Chain-3 length: {}", chain3.length);

        // Verify all certificates in the final chain
        for (int i = 0; i < chain3.length - 1; i++) {
            final int index = i;
            assertDoesNotThrow(() -> chain3[index].verify(chain3[index + 1].getPublicKey()),
                    "Certificate at index " + index + " should be signed by certificate at index " + (index + 1));
        }
    }

    private static boolean containsIgnoringLines(String a, String b) {
        return joinLines(a).contains(joinLines(b));
    }
}

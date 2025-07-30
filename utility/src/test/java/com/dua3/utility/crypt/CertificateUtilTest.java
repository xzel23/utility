// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CertificateUtilTest {

    @Test
    void testCreateSelfSignedX509Certificate() throws GeneralSecurityException {
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
        for (String part: subject.split("[,\n]")) {
            String strippedPart = part.strip();
            assertTrue(certificates[0].toString().contains(strippedPart), "Certificate string should contain '" + strippedPart + "'");
        }
    }

    @Test
    void testCreateX509Certificate() throws GeneralSecurityException {
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
    }

    /**
     * Helper method to create a self-signed parent certificate for testing.
     */
    private Map<String, Object> createTestParentCertificate() throws GeneralSecurityException {
        KeyPair parentKeyPair = KeyUtil.generateRSAKeyPair();
        String parentSubject = "CN=Parent, O=Test Organization, C=US";
        int parentValidityDays = 365;

        X509Certificate[] parentCertificates = CertificateUtil.createSelfSignedX509Certificate(
                parentKeyPair, parentSubject, parentValidityDays, true);

        return Map.of(
                "certificate", parentCertificates[0],
                "privateKey", parentKeyPair.getPrivate()
        );
    }

    @Test
    void testToX509Certificate() throws GeneralSecurityException {
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
    void testToX509CertificateFromPemString() throws GeneralSecurityException {
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
        X509Certificate convertedCertificate = CertificateUtil.toX509Certificate(pemData);
        assertNotNull(convertedCertificate, "The converted certificate should not be null.");
        assertArrayEquals(originalCertificate.getEncoded(), convertedCertificate.getEncoded(), "The certificate bytes should match.");
        assertTrue(convertedCertificate.toString().contains("CN=Test PEM Certificate"), "The certificate subject should match the original.");

        // Test with invalid PEM data
        String invalidPem = "-----BEGIN CERTIFICATE-----\nInvalid PEM Data\n-----END CERTIFICATE-----";
        assertThrows(CertificateException.class, () -> CertificateUtil.toX509Certificate(invalidPem));
    }

    @Test
    void testCreateX509CertificateWithParentChain() throws GeneralSecurityException {
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
    }

    @Test
    void testToCertificateChain() throws GeneralSecurityException {
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
        X509Certificate[] chain = CertificateUtil.toCertificateChain(concatenatedPem);
        assertNotNull(chain, "The certificate chain should not be null.");
        assertEquals(2, chain.length, "The certificate chain should contain 2 certificates.");
        assertEquals(intermediateCertificate, chain[0], "The first certificate in the chain should be the intermediate.");
        assertEquals(rootCertificate, chain[1], "The second certificate in the chain should be the root.");
    }

    @Test
    void testToCertificateChainWithInvalidData() {
        // Invalid PEM data with missing end marker
        String invalidPem = "-----BEGIN CERTIFICATE-----\nInvalid Data";
        assertThrows(IllegalStateException.class, () -> CertificateUtil.toCertificateChain(invalidPem),
                "An exception should be thrown for invalid PEM data.");
    }

    @Test
    void testToCertificateChainWithEmptyData() throws GeneralSecurityException {
        // Invalid PEM data does not contain certificates
        String emptyPem = "";
        assertEquals(0, CertificateUtil.toCertificateChain(emptyPem).length, "Chain should have 0 length for empty PEM data.");
    }
}

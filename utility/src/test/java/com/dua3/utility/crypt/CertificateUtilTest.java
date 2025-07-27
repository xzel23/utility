// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CertificateUtilTest {
    private static final Logger LOG = LogManager.getLogger(CertificateUtilTest.class);

    static {
        // Register Bouncy Castle provider for tests
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            LOG.error("Failed to register Bouncy Castle provider: {}", e.getMessage());
        }
    }

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
                childKeyPair, subject, validityDays, parentCertificate, parentPrivateKey);

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

}

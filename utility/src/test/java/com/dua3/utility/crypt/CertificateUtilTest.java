// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.*;

class CertificateUtilTest {

    static {
        // Register Bouncy Castle provider for tests
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            System.err.println("Failed to register Bouncy Castle provider: " + e.getMessage());
        }
    }

    @Test
    void testCreateSelfSignedX509Certificate() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        String subject = "CN=Test Subject, O=Test Organization, C=US";
        int validityDays = 365;

        // Create a self-signed certificate
        Certificate[] certificates = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays);

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
}
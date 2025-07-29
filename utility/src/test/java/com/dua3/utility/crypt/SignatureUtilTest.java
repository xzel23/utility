// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.bouncycastle.cms.CMSSignedData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureUtilTest {

    private static final String[] MESSAGES = {
            "",
            "secret message",
            System.getProperties().toString()
    };

    @Test
    void testSigningAndVerification() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array signing/verification
        for (String message : MESSAGES) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            byte[] signature = SignatureUtil.sign(privateKey, data, InputBufferHandling.PRESERVE);
            boolean verified = SignatureUtil.verify(publicKey, data, signature, InputBufferHandling.CLEAR_AFTER_USE);

            assertTrue(verified);

            // Verify that modifying the data invalidates the signature
            if (data.length > 0) {
                byte[] modifiedData = Arrays.copyOf(data, data.length);
                modifiedData[0] = (byte) (modifiedData[0] + 1);
                boolean verifiedModified = SignatureUtil.verify(publicKey, modifiedData, signature, InputBufferHandling.CLEAR_AFTER_USE);
                assertFalse(verifiedModified);
            }
        }

        // Test string signing/verification
        String message = "Test message for signing";
        byte[] signature = SignatureUtil.sign(privateKey, message);
        boolean verified = SignatureUtil.verify(publicKey, message, signature);

        assertTrue(verified);

        // Test char array signing/verification
        byte[] signatureChars = SignatureUtil.sign(privateKey, message.toCharArray(), InputBufferHandling.CLEAR_AFTER_USE);
        boolean verifiedChars = SignatureUtil.verify(publicKey, message.toCharArray(), signatureChars, InputBufferHandling.CLEAR_AFTER_USE);

        assertTrue(verifiedChars);
    }

    @Test
    void testCreateSignedData() throws Exception {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        // Generate a self-signed certificate for the test
        X509Certificate[] certificate = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 365, false);

        // Test data to be signed
        byte[] certificateBytes = "Sample certificate data".getBytes(StandardCharsets.UTF_8);

        // Call createSignedData and validate the result
        CMSSignedData signedData = SignatureUtil.createSignedData(privateKey, certificate[certificate.length - 1], certificateBytes);
        assertNotNull(signedData);
        assertNotNull(signedData.getEncoded());

        // Validate the basic structure of the signed data
        byte[] signedContent = (byte[]) signedData.getSignedContent().getContent();
        assertArrayEquals(certificateBytes, signedContent);
    }
}

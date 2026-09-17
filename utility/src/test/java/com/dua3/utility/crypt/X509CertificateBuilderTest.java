package com.dua3.utility.crypt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class X509CertificateBuilderTest {

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testGetBuilder() {
        Optional<X509CertificateBuilder> caBuilder = X509CertificateBuilder.getBuilder(true);
        assertTrue(caBuilder.isPresent());

        Optional<X509CertificateBuilder> nonCaBuilder = X509CertificateBuilder.getBuilder(false);
        assertTrue(nonCaBuilder.isPresent());
    }

    @Test
    void testValidityDaysValidation() {
        X509CertificateBuilder builder = X509CertificateBuilder.getBuilder(false).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> builder.validityDays(0));
        assertThrows(IllegalArgumentException.class, () -> builder.validityDays(-10));
    }

    @Test
    void testBuildWithoutSubject() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        X509CertificateBuilder builder = X509CertificateBuilder.getBuilder(false).orElseThrow();
        assertThrows(IllegalStateException.class, () -> builder.build(kp));
    }

    @Test
    void testBuildSelfSignedCAAndNonCA() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();

        // CA Certificate
        X509Certificate[] caChain = X509CertificateBuilder.getBuilder(true).orElseThrow()
                .subject("CN=CA Certificate, O=Test, C=US")
                .validityDays(365)
                .signatureAlgorithm("SHA256withRSA")
                .build(kp);

        assertNotNull(caChain);
        assertEquals(1, caChain.length);
        assertNotEquals(-1, caChain[0].getBasicConstraints(), "CA certificate basicConstraints should be >= 0");
        boolean[] caKeyUsage = caChain[0].getKeyUsage();
        assertNotNull(caKeyUsage);
        assertTrue(caKeyUsage[5], "KeyUsage keyCertSign (bit 5) should be true for CA");

        // Non-CA Certificate
        X509Certificate[] nonCaChain = X509CertificateBuilder.getBuilder(false).orElseThrow()
                .subject("CN=End Entity, O=Test, C=US")
                .validityDays(180)
                .build(kp);

        assertNotNull(nonCaChain);
        assertEquals(1, nonCaChain.length);
        assertEquals(-1, nonCaChain[0].getBasicConstraints(), "Non-CA certificate basicConstraints should be -1");
        boolean[] nonCaKeyUsage = nonCaChain[0].getKeyUsage();
        assertNotNull(nonCaKeyUsage);
        assertFalse(nonCaKeyUsage[5], "KeyUsage keyCertSign (bit 5) should be false for non-CA");
    }

    @Test
    void testBuildWithIssuerAndSignedBy() throws Exception {
        KeyPair caKp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] caChain = X509CertificateBuilder.getBuilder(true).orElseThrow()
                .subject("CN=Issuer CA, O=Test, C=US")
                .validityDays(365)
                .build(caKp);

        KeyPair childKp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] childChain = X509CertificateBuilder.getBuilder(false).orElseThrow()
                .subject("CN=Child Service, O=Test, C=US")
                .issuer("CN=Issuer CA, O=Test, C=US")
                .validityDays(90)
                .signedBy(caKp.getPrivate(), caChain[0])
                .build(childKp);

        assertNotNull(childChain);
        assertEquals(2, childChain.length);
        assertEquals("CN=Child Service,O=Test,C=US", childChain[0].getSubjectX500Principal().getName());
        assertEquals("CN=Issuer CA,O=Test,C=US", childChain[0].getIssuerX500Principal().getName());
        assertEquals(caChain[0], childChain[1]);
    }
}

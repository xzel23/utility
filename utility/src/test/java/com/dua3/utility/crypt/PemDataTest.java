package com.dua3.utility.crypt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PemDataTest {

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testParseSingleCertificate() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] certs = CertificateUtil.createSelfSignedX509Certificate(kp, "CN=TestCert", 365, false);
        String pem = CertificateUtil.toPem(certs[0]);

        PemData pemData = PemData.parse(pem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.CERTIFICATE, pemData.get(0).type());
        assertEquals(certs[0], pemData.asCertificate());

        List<X509Certificate> chain = pemData.asCertificateChain();
        assertEquals(1, chain.size());
        assertEquals(certs[0], chain.getFirst());

        X509Certificate[] chainArray = pemData.asCertificateChainArray();
        assertEquals(1, chainArray.length);
        assertEquals(certs[0], chainArray[0]);

        List<PemData.PemItem> certItems = pemData.getItems(PemData.PemType.CERTIFICATE);
        assertEquals(1, certItems.size());
        assertEquals(0, pemData.getItems(PemData.PemType.PUBLIC_KEY).size());

        assertEquals(1, pemData.toList().size());
        assertTrue(pemData.iterator().hasNext());
    }

    @Test
    void testParseCertificateChain() throws Exception {
        KeyPair caKp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] caCerts = CertificateUtil.createSelfSignedX509Certificate(caKp, "CN=RootCA", 365, true);

        KeyPair childKp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] certChain = CertificateUtil.createX509Certificate(
                childKp, "CN=ChildCert", 180, false, caKp.getPrivate(), caCerts[0]);

        String chainPem = CertificateUtil.toPem(certChain);
        PemData pemData = PemData.parse(chainPem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.CERTIFICATE_CHAIN, pemData.get(0).type());

        List<X509Certificate> loadedChain = pemData.asCertificateChain();
        assertEquals(2, loadedChain.size());
        assertEquals(certChain[0], loadedChain.get(0));
        assertEquals(certChain[1], loadedChain.get(1));

        X509Certificate[] chainArray = pemData.asCertificateChainArray();
        assertEquals(2, chainArray.length);

        // asCertificate() should fail for a chain
        assertThrows(PemData.PemException.class, pemData::asCertificate);
    }

    @Test
    void testParsePublicKey() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        String pem = KeyUtil.toPem(kp.getPublic());

        PemData pemData = PemData.parse(pem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.PUBLIC_KEY, pemData.get(0).type());

        PublicKey pubKey = pemData.asPublicKey();
        assertNotNull(pubKey);
        assertArrayEquals(kp.getPublic().getEncoded(), pubKey.getEncoded());

        // asPrivateKey / asCertificate / asKeyPair should fail
        assertThrows(PemData.PemException.class, pemData::asPrivateKey);
        assertThrows(PemData.PemException.class, () -> pemData.asPrivateKey("pwd".toCharArray()));
        assertThrows(PemData.PemException.class, pemData::asCertificate);
        assertThrows(PemData.PemException.class, pemData::asKeyPair);
    }

    @Test
    void testParsePkcs8PrivateKey() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        java.io.StringWriter sw = new java.io.StringWriter();
        try (org.bouncycastle.openssl.jcajce.JcaPEMWriter writer = new org.bouncycastle.openssl.jcajce.JcaPEMWriter(sw)) {
            writer.writeObject(new org.bouncycastle.openssl.jcajce.JcaPKCS8Generator(kp.getPrivate(), null));
        }
        String pem = sw.toString();

        PemData pemData = PemData.parse(pem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.PRIVATE_KEY, pemData.get(0).type());

        PrivateKey privKey = pemData.asPrivateKey();
        assertNotNull(privKey);
        assertArrayEquals(kp.getPrivate().getEncoded(), privKey.getEncoded());

        // asPrivateKey with password also works for unencrypted private key
        PrivateKey privKeyWithPwd = pemData.asPrivateKey("pwd".toCharArray());
        assertNotNull(privKeyWithPwd);
        assertArrayEquals(kp.getPrivate().getEncoded(), privKeyWithPwd.getEncoded());

        // asPublicKey should fail
        assertThrows(PemData.PemException.class, pemData::asPublicKey);
        // asKeyPair should fail
        assertThrows(PemData.PemException.class, pemData::asKeyPair);
    }

    @Test
    void testParseKeyPair() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        StringBuilder sb = new StringBuilder();
        KeyUtil.appendPem(kp, sb);
        String pem = sb.toString();

        PemData pemData = PemData.parse(pem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.KEY_PAIR, pemData.get(0).type());

        KeyPair loadedKp = pemData.asKeyPair();
        assertNotNull(loadedKp);
        assertArrayEquals(kp.getPublic().getEncoded(), loadedKp.getPublic().getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedKp.getPrivate().getEncoded());

        // asKeyPair with password also works
        KeyPair loadedKpWithPwd = pemData.asKeyPair("pwd".toCharArray());
        assertNotNull(loadedKpWithPwd);
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedKpWithPwd.getPrivate().getEncoded());

        // asPublicKey and asPrivateKey extracted from key pair
        PublicKey pub = pemData.asPublicKey();
        assertArrayEquals(kp.getPublic().getEncoded(), pub.getEncoded());

        PrivateKey priv = pemData.asPrivateKey();
        assertArrayEquals(kp.getPrivate().getEncoded(), priv.getEncoded());
    }

    @SuppressWarnings("java:S5778")
    @Test
    void testParseEncryptedPrivateKey() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        char[] password = "secretPassword".toCharArray();
        String pem = KeyUtil.toPem(kp.getPrivate(), password.clone());

        PemData pemData = PemData.parse(pem);

        assertEquals(1, pemData.size());
        assertEquals(PemData.PemType.ENCRYPTED_PRIVATE_KEY, pemData.get(0).type());

        // asPrivateKey without password throws PemException
        assertThrows(PemData.PemException.class, pemData::asPrivateKey);

        // asPrivateKey with correct password succeeds
        PrivateKey decrypted = pemData.asPrivateKey("secretPassword".toCharArray());
        assertNotNull(decrypted);
        assertArrayEquals(kp.getPrivate().getEncoded(), decrypted.getEncoded());

        // Decryption with wrong password throws IllegalStateException
        assertThrows(IllegalStateException.class, () -> pemData.asPrivateKey("wrongPassword".toCharArray()));
    }

    @SuppressWarnings("java:S5778")
    @Test
    void testParseTwoItemPkcs8KeyPair() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        java.io.StringWriter sw = new java.io.StringWriter();
        try (org.bouncycastle.openssl.jcajce.JcaPEMWriter writer = new org.bouncycastle.openssl.jcajce.JcaPEMWriter(sw)) {
            writer.writeObject(new org.bouncycastle.openssl.jcajce.JcaPKCS8Generator(kp.getPrivate(), null));
            writer.writeObject(kp.getPublic());
        }

        PemData pemData = PemData.parse(sw.toString());
        assertEquals(2, pemData.size());
        assertEquals(PemData.PemType.PRIVATE_KEY, pemData.get(0).type());
        assertEquals(PemData.PemType.PUBLIC_KEY, pemData.get(1).type());

        KeyPair loaded = pemData.asKeyPair("ignored".toCharArray());
        assertNotNull(loaded);
        assertArrayEquals(kp.getPublic().getEncoded(), loaded.getPublic().getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getPrivate().getEncoded());

        // asKeyPair without password expects size 1
        assertThrows(PemData.PemException.class, pemData::asKeyPair);
        // asPublicKey and asPrivateKey expect size 1
        assertThrows(IllegalStateException.class, pemData::asPublicKey);
        assertThrows(IllegalStateException.class, pemData::asPrivateKey);
        assertThrows(IllegalStateException.class, () -> pemData.asPrivateKey("ignored".toCharArray()));
    }

    @Test
    void testParseTwoItemEncryptedKeyPair() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        StringBuilder sb = new StringBuilder();
        // Append encrypted private key then public key
        KeyUtil.appendPem(kp.getPrivate(), "secret".toCharArray(), sb);
        sb.append('\n');
        KeyUtil.appendPem(kp.getPublic(), sb);

        PemData pemData = PemData.parse(sb.toString());
        assertEquals(2, pemData.size());

        KeyPair loaded = pemData.asKeyPair("secret".toCharArray());
        assertNotNull(loaded);
        assertArrayEquals(kp.getPublic().getEncoded(), loaded.getPublic().getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getPrivate().getEncoded());
    }

    @SuppressWarnings("java:S5778")
    @Test
    void testEmptyPemData() throws Exception {
        PemData pemData = PemData.parse("");
        assertEquals(0, pemData.size());
        assertTrue(pemData.asCertificateChain().isEmpty());
        assertEquals(0, pemData.asCertificateChainArray().length);
        assertThrows(PemData.PemException.class, pemData::asCertificate);
        assertThrows(IllegalStateException.class, pemData::asPublicKey);
        assertThrows(IllegalStateException.class, pemData::asPrivateKey);
        assertThrows(IllegalStateException.class, () -> pemData.asPrivateKey("pwd".toCharArray()));
        assertThrows(PemData.PemException.class, pemData::asKeyPair);
        assertThrows(PemData.PemException.class, () -> pemData.asKeyPair("pwd".toCharArray()));
    }

    @Test
    void testLoadFromReaderInputStreamAndPath(@TempDir Path tempDir) throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        X509Certificate[] certs = CertificateUtil.createSelfSignedX509Certificate(kp, "CN=TestFileCert", 365, false);
        String pem = CertificateUtil.toPem(certs[0]);

        // Load via Reader
        try (StringReader reader = new StringReader(pem)) {
            PemData fromReader = PemData.load(reader);
            assertEquals(1, fromReader.size());
            assertEquals(certs[0], fromReader.asCertificate());
        }

        // Load via InputStream
        try (ByteArrayInputStream in = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
            PemData fromIn = PemData.load(in);
            assertEquals(1, fromIn.size());
            assertEquals(certs[0], fromIn.asCertificate());
        }

        // Load via Path
        Path pemFile = tempDir.resolve("test.pem");
        Files.writeString(pemFile, pem, StandardCharsets.UTF_8);
        PemData fromPath = PemData.load(pemFile);
        assertEquals(1, fromPath.size());
        assertEquals(certs[0], fromPath.asCertificate());
    }

    @Test
    void testPemExceptions() {
        PemData.PemException ex1 = new PemData.PemException("message");
        assertEquals("message", ex1.getMessage());

        Exception cause = new RuntimeException("root cause");
        PemData.PemException ex2 = new PemData.PemException(cause);
        assertSame(cause, ex2.getCause());

        PemData.PemException ex3 = new PemData.PemException("message with cause", cause);
        assertEquals("message with cause", ex3.getMessage());
        assertSame(cause, ex3.getCause());
    }
}

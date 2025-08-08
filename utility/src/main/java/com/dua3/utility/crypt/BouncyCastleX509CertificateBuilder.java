package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jspecify.annotations.Nullable;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * The {@code BouncyCastleX509CertificateBuilder} is an implementation of the {@code X509CertificateBuilder}
 * interface that provides functionality to create and configure X.509 certificates. This builder uses the
 * Bouncy Castle library for certificate generation and offers methods to specify certificate details such as
 * the subject and issuer, validity period, signature algorithm, and certificate chain.
 */
final class BouncyCastleX509CertificateBuilder implements X509CertificateBuilder {

    private static final @Nullable X509Certificate[] EMPTY_CERTIFICATE_ARRAY = {};

    private final boolean enableCA;
    private @Nullable X500Principal subject;
    private @Nullable X500Principal issuer;
    private int validityDays = 365;
    private String signatureAlgorithm = "SHA256withRSA";

    private @Nullable X509Certificate[] issuerCert = EMPTY_CERTIFICATE_ARRAY;
    private @Nullable PrivateKey issuerKey;

    /**
     * Creates and returns an optional instance of {@code X509CertificateBuilder}.
     * This method provides a new instance of {@code BouncyCastleX509CertificateBuilder}
     * if the required cryptographic provider is configured, enabling X.509 certificate
     * creation and customization.
     *
     * @return an {@code Optional} containing an instance of {@code X509CertificateBuilder}
     *         if the required cryptographic provider is available; otherwise, an empty
     *         {@code Optional}.
     */
    public static Optional<X509CertificateBuilder> create(boolean enableCA) {
        return !BouncyCastle.isAvailable() ? Optional.empty() : Optional.of(new BouncyCastleX509CertificateBuilder(enableCA));
    }

    /**
     * Private default constructor for the {@code BouncyCastleX509CertificateBuilder} class.
     * <p>
     * This constructor prevents external instantiation of the {@code BouncyCastleX509CertificateBuilder}
     * class. Access to an instance is provided via the {@link #create(boolean)} method.
     */
    private BouncyCastleX509CertificateBuilder(boolean enableCA) {
        this.enableCA = enableCA;
    }

    @Override
    public BouncyCastleX509CertificateBuilder signatureAlgorithm(String algorithm) {
        this.signatureAlgorithm = algorithm;
        return this;
    }

    @Override
    public BouncyCastleX509CertificateBuilder signedBy(PrivateKey issuerKey, X509Certificate... issuerCert) {
        this.issuerCert = issuerCert;
        this.issuerKey = issuerKey;
        return this;
    }

    @Override
    public X509CertificateBuilder subject(String dn) {
        this.subject = new javax.security.auth.x500.X500Principal(dn);
        return this;
    }

    @Override
    public X509CertificateBuilder issuer(String dn) {
        this.issuer = new javax.security.auth.x500.X500Principal(dn);
        return this;
    }

    @Override
    public X509CertificateBuilder validityDays(int days) {
        LangUtil.checkArg("days", d -> d > 0, days);
        this.validityDays = days;
        return this;
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Override
    public X509Certificate[] build(KeyPair keyPair) throws GeneralSecurityException {
        if (subject == null) {
            throw new IllegalStateException("subject DN not set");
        }
        if (issuer == null && issuerCert.length == 0) {
            issuer = subject;
        }

        Provider provider = BouncyCastle.ensureProvider();

        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plusSeconds(validityDays * 86400L));
        BigInteger serial = new BigInteger(64, new SecureRandom());

        PrivateKey signingKey = (issuerKey != null) ? issuerKey : keyPair.getPrivate();

        try {
            ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm)
                    .setProvider(provider)
                    .build(signingKey);

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuer,
                    serial,
                    notBefore,
                    notAfter,
                    subject,
                    keyPair.getPublic()
            );

            // Add X.509 extensions
            certBuilder.addExtension(Extension.basicConstraints, true,
                    new BasicConstraints(enableCA));

            certBuilder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | (enableCA ? KeyUsage.keyCertSign : 0)));

            // Subject Key Identifier
            SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                    new SubjectKeyIdentifier(pubKeyInfo.getPublicKeyData().getBytes()));

            // Authority Key Identifier (if issuerCert is provided)
            if (issuerCert.length > 0) {
                certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                        new AuthorityKeyIdentifier(issuerCert[0].getPublicKey().getEncoded()));
            }

            X509Certificate cert = new JcaX509CertificateConverter()
                    .setProvider(provider)
                    .getCertificate(certBuilder.build(signer));

            X509Certificate[] chain = new X509Certificate[issuerCert.length + 1];
            chain[0] = cert;
            System.arraycopy(issuerCert, 0, chain, 1, issuerCert.length);

            CertificateUtil.verifyCertificateChain(chain);

            return chain;
        } catch (OperatorCreationException | CertIOException e) {
            throw new CertificateException("Failed to create certificate", e);
        }
    }
}

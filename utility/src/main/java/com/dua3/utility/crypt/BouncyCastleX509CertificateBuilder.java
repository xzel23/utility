package com.dua3.utility.crypt;

import org.bouncycastle.asn1.x500.X500Name;
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

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The {@code BouncyCastleX509CertificateBuilder} is an implementation of the {@code X509CertificateBuilder}
 * interface that provides functionality to create and configure X.509 certificates. This builder uses the
 * Bouncy Castle library for certificate generation and offers methods to specify certificate details such as
 * the subject and issuer, validity period, signature algorithm, and certificate chain.
 */
final class BouncyCastleX509CertificateBuilder implements X509CertificateBuilder {

    private static final @Nullable Provider provider = Security.getProvider("BC");

    private final boolean enableCA;
    private @Nullable String subjectDn;
    private @Nullable String issuerDn;
    private int validityDays = 365;
    private String signatureAlgorithm = "SHA256withRSA";

    private @Nullable X509Certificate issuerCert;
    private @Nullable PrivateKey issuerKey;
    private final List<X509Certificate> chain = new ArrayList<>();

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
        return provider == null ? Optional.empty() : Optional.of(new BouncyCastleX509CertificateBuilder(enableCA));
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
    public BouncyCastleX509CertificateBuilder signedBy(X509Certificate issuerCert, PrivateKey issuerKey) {
        this.issuerCert = issuerCert;
        this.issuerKey = issuerKey;
        return this;
    }

    @Override
    public BouncyCastleX509CertificateBuilder addToChain(X509Certificate... additionalCerts) {
        this.chain.addAll(List.of(additionalCerts));
        return this;
    }

    @Override
    public X509CertificateBuilder subject(String dn) {
        this.subjectDn = dn;
        return this;
    }

    @Override
    public X509CertificateBuilder issuer(String dn) {
        this.issuerDn = dn;
        return this;
    }

    @Override
    public X509CertificateBuilder validityDays(int days) {
        this.validityDays = days;
        return this;
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Override
    public X509Certificate[] build(KeyPair keyPair) throws GeneralSecurityException {
        if (subjectDn == null) throw new IllegalStateException("subject DN not set");
        if (issuerDn == null && issuerCert == null) issuerDn = subjectDn;

        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plusSeconds(validityDays * 86400L));
        BigInteger serial = new BigInteger(64, new SecureRandom());

        X500Name subject = new X500Name(subjectDn);
        X500Name issuer = (issuerCert != null)
                ? new X500Name(issuerCert.getSubjectX500Principal().getName())
                : new X500Name(issuerDn);

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
            if (issuerCert != null) {
                certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                        new AuthorityKeyIdentifier(issuerCert.getPublicKey().getEncoded()));
            }

            X509Certificate cert = new JcaX509CertificateConverter()
                    .setProvider(provider)
                    .getCertificate(certBuilder.build(signer));

            List<X509Certificate> fullChain = new ArrayList<>();
            fullChain.add(cert);
            if (issuerCert != null) fullChain.add(issuerCert);
            fullChain.addAll(chain);
            return fullChain.toArray(new X509Certificate[0]);

        } catch (OperatorCreationException | CertIOException e) {
            throw new CertificateException("Failed to create certificate", e);
        }
    }
}

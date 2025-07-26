package com.dua3.utility.crypt;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Interface for building custom X.509 certificates. It provides methods
 * to specify certificate details such as subject, issuer, validity period,
 * and signature parameters. The builder also allows adding certificates
 * to a chain and signing the resulting certificate with a provided key.
 */
public interface X509CertificateBuilder {
    /**
     * Sets the subject distinguished name (DN) for the X.509 certificate.
     * The subject DN identifies the entity associated with the certificate.
     *
     * @param dn the distinguished name of the subject, formatted as
     *           a standard X.500 DN string (e.g., "CN=Subject, O=Organization, C=Country").
     * @return the current instance of {@code X509CertificateBuilder} for method chaining.
     */
    X509CertificateBuilder subject(String dn);

    /**
     * Sets the issuer's distinguished name (DN) for the X.509 certificate.
     *
     * @param dn the distinguished name of the issuer in the X.509 certificate.
     *           This should follow the format of a standard X.509 DN string,
     *           such as "CN=Issuer Name, O=Organization, C=Country".
     * @return the current instance of {@code X509CertificateBuilder} for method chaining.
     */
    X509CertificateBuilder issuer(String dn);

    /**
     * Sets the validity period of the certificate by specifying the number of days
     * from the current date that the certificate should remain valid. This determines
     * the certificate's "Not After" date.
     *
     * @param days the number of days from the current date for which the certificate will be valid
     * @return the current instance of {@code X509CertificateBuilder} for method chaining
     */
    X509CertificateBuilder validityDays(int days);

    /**
     * Builds an array of X.509 certificates based on the current configuration of the builder.
     * The first certificate in the array is the newly created certificate, followed
     * by any certificates added to the chain. The resulting certificates are signed
     * using the private key provided in the given key pair.
     *
     * @param keyPair the key pair containing the private key used to sign the certificate
     *                and the public key embedded in the certificate.
     * @return an array of X.509 certificates, including the new certificate and any
     *         additional certificates in the chain.
     * @throws GeneralSecurityException if an error occurs during certificate generation
     *         or signing, such as invalid parameters or unsupported operations.
     */
    X509Certificate[] build(KeyPair keyPair) throws GeneralSecurityException;

    /**
     * Specifies the signature algorithm to be used for signing the certificate.
     *
     * @param algorithm the name of the signature algorithm (e.g., "SHA256withRSA" or "SHA256withECDSA").
     *                  This must be a valid algorithm supported by the cryptographic provider.
     * @return the updated {@code X509CertificateBuilder} instance, allowing for method chaining.
     */
    X509CertificateBuilder signatureAlgorithm(String algorithm);

    /**
     * Specifies the issuer certificate and private key to sign the certificate being built.
     * This method is used to define the signer of the resulting certificate in the chain.
     *
     * @param issuerCert the X.509 certificate of the issuer that will sign the generated certificate.
     *                   This certificate indicates the identity of the signing authority.
     * @param issuerKey the private key corresponding to the issuer certificate. It is used
     *                  to generate the signature for the certificate being built.
     * @return the current instance of {@code X509CertificateBuilder} for method chaining.
     */
    X509CertificateBuilder signedBy(X509Certificate issuerCert, PrivateKey issuerKey);

    /**
     * Adds one or more X.509 certificates to the certificate chain being built.
     * This method can be used to include intermediate or root certificates
     * that are part of the chain of trust.
     *
     * @param additionalCerts one or more certificates to add to the chain.
     *                        These certificates should typically be intermediate
     *                        or root certificates that establish the trust chain.
     * @return the current instance of {@code X509CertificateBuilder}, allowing for
     *         method chaining to continue building the certificate.
     */
    X509CertificateBuilder addToChain(X509Certificate... additionalCerts);

    /**
     * Returns an {@code Optional} containing an instance of {@code X509CertificateBuilder},
     * used for constructing X.509 certificates. The builder can be configured for creating
     * either a Certificate Authority (CA) certificate or a regular certificate.
     *
     * @param enableCA a boolean flag indicating whether to enable Certificate Authority (CA) mode.
     *                 If {@code true}, the builder will configure the certificate as a CA.
     *                 If {@code false}, the builder will configure the certificate as a non-CA.
     * @return an {@code Optional} containing a configured {@code X509CertificateBuilder} instance,
     *         or an empty {@code Optional} if the creation of the builder fails.
     */
    static Optional<X509CertificateBuilder> getBuilder(boolean enableCA) {
        return BouncyCastleX509CertificateBuilder.create(enableCA);
    }
}

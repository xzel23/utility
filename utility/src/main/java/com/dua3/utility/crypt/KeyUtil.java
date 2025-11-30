package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.jspecify.annotations.Nullable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;

/**
 * Utility class for cryptographic key operations.
 */
public final class KeyUtil {

    /**
     * Defines the minimum key size, in bits, required for RSA and DSA cryptographic algorithms.
     * This value ensures compliance with security standards by specifying a minimal acceptable
     * level of cryptographic strength for keys used in RSA and DSA operations. Keys smaller
     * than this size are considered insecure and should not be used.
     */
    private static final int RSA_DSA_MINIMAL_KEY_SIZE = 2048;
    /**
     * Defines the set of allowed elliptic curve (EC) key sizes for cryptographic operations.
     * <p>
     * This array contains the specific key sizes in bits that are permissible for use
     * within the system to ensure compliance with predefined security policies.
     * Key sizes included in the array are chosen based on cryptographic strength
     * and commonly accepted standards for elliptic curve cryptography.
     */
    private static final SequencedCollection<Integer> EC_ALLOWED_KEY_SIZES = new LinkedHashSet<>(Set.of(256, 384, 521));

    /**
     * Utility class private constructor.
     */
    private KeyUtil() { /* utility class */ }

    /**
     * Generate random salt for key derivation.
     *
     * @param length salt length in bytes (recommended: 16 or 32)
     * @return random salt
     */
    public static byte[] generateSalt(int length) {
        return RandomUtil.generateRandomBytes(length);
    }

    /**
     * Validates a symmetric key for use with the specified algorithm.
     *
     * @param key       the key to validate
     * @param algorithm the algorithm the key will be used with
     * @throws GeneralSecurityException if the key is not compatible or key validation fails
     */
    public static void validateSymmetricKey(Key key, SymmetricAlgorithm algorithm) throws GeneralSecurityException {
        if (!algorithm.getKeyAlgorithm().equals(key.getAlgorithm())) {
            throw new InvalidKeyException(
                    String.format("Key algorithm mismatch. Expected: %s, got: %s",
                            algorithm.getKeyAlgorithm(), key.getAlgorithm()));
        }

        if (key.getEncoded() != null) {
            algorithm.validateKeySize(key.getEncoded().length * 8);
        }
    }

    /**
     * Create a SecretKey from a byte array using the default algorithm.
     *
     * @param keyBytes the key bytes (must be 128, 192, or 256 bits for AES)
     * @return SecretKey instance
     * @throws InvalidAlgorithmParameterException if the bytes could not be converted to a secret key
     */
    public static SecretKey toSecretKey(byte[] keyBytes) throws InvalidAlgorithmParameterException {
        return toSecretKey(keyBytes, SymmetricAlgorithm.AES);
    }

    /**
     * Converts the provided byte array into a {@link SecretKey} using the specified symmetric algorithm.
     *
     * @param keyBytes  the byte array representing the key material
     * @param algorithm the symmetric algorithm used to validate the key size and determine the key algorithm
     * @return a {@link SecretKey} generated from the provided key material and algorithm
     * @throws InvalidAlgorithmParameterException if the key size is invalid for the specified algorithm
     */
    public static SecretKey toSecretKey(byte[] keyBytes, SymmetricAlgorithm algorithm) throws InvalidAlgorithmParameterException {
        algorithm.validateKeySize(keyBytes.length * 8);
        return new SecretKeySpec(keyBytes, algorithm.getKeyAlgorithm());
    }

    /**
     * Generate SecretKey using the default algorithm.
     *
     * @param bits the number of bits (128, 192, or 256 for AES)
     * @return the generated SecretKey
     * @throws GeneralSecurityException if key generation fails
     */
    public static SecretKey generateSecretKey(int bits) throws GeneralSecurityException {
        return generateSecretKey(bits, SymmetricAlgorithm.AES);
    }

    /**
     * Generate SecretKey for the specified algorithm.
     *
     * @param bits      the number of bits
     * @param algorithm the symmetric algorithm
     * @return the generated SecretKey
     * @throws GeneralSecurityException if key generation fails
     */
    public static SecretKey generateSecretKey(int bits, SymmetricAlgorithm algorithm) throws GeneralSecurityException {
        algorithm.validateKeySize(bits);

        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm.getKeyAlgorithm());
        keyGen.init(bits, RandomUtil.getRandom());
        return keyGen.generateKey();
    }

    /**
     * Converts a byte array representing a private key into a {@code PrivateKey} object
     * using the default key generation algorithm.
     *
     * @param bytes the byte array containing the encoded private key
     * @return the generated {@code PrivateKey} object
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PrivateKey toPrivateKey(byte[] bytes) throws GeneralSecurityException {
        return toPrivateKey(bytes, AsymmetricAlgorithm.RSA);
    }

    /**
     * Converts a {@link PrivateKeyInfo} object into a {@link PrivateKey} instance.
     *
     * @param pki the {@link PrivateKeyInfo} containing the private key data and algorithm information.
     * @return a {@link PrivateKey} created from the provided {@link PrivateKeyInfo}.
     * @throws IOException if an error occurs during parsing or encoding of the private key.
     * @throws GeneralSecurityException if an error occurs while generating the private key.
     */
    public static PrivateKey toPrivateKey(PrivateKeyInfo pki) throws IOException, GeneralSecurityException {
        String algOid = pki.getPrivateKeyAlgorithm().getAlgorithm().getId();
        if (algOid.equals("1.2.840.113549.1.1.1")) { // RSA
            // Convert PKCS#8 -> PKCS#1 fields
            RSAPrivateKey rsa = RSAPrivateKey.getInstance(pki.parsePrivateKey());
            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
                    rsa.getModulus(),
                    rsa.getPublicExponent(),
                    rsa.getPrivateExponent(),
                    rsa.getPrime1(),
                    rsa.getPrime2(),
                    rsa.getExponent1(),
                    rsa.getExponent2(),
                    rsa.getCoefficient()
            );
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } else {
            if (algOid.equals("1.2.840.10045.2.1") && pki.getPrivateKeyAlgorithm().getParameters() == null) { // EC
                    throw new InvalidKeyException("EC domain parameters must be encoded in the algorithm identifier");
            }
            // Other algorithms (DSA, etc.)
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(pki);
        }
    }

    /**
     * Converts a given byte array into a {@code PrivateKey} instance using the specified algorithm.
     *
     * @param bytes     the private key in encoded byte format
     * @param algorithm the asymmetric algorithm
     * @return a {@code PrivateKey} created from the provided byte array and algorithm
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PrivateKey toPrivateKey(byte[] bytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm.keyFactoryAlgorithm());
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Converts a byte array into a {@code PublicKey} using the default key generation algorithm.
     *
     * @param bytes the byte array representing the public key
     * @return the corresponding {@code PublicKey} instance
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PublicKey toPublicKey(byte[] bytes) throws GeneralSecurityException {
        return toPublicKey(bytes, AsymmetricAlgorithm.RSA);
    }

    /**
     * Converts the provided byte array into a {@code PublicKey} instance using the specified algorithm.
     *
     * @param bytes     the byte array containing the key data
     * @param algorithm the asymmetric algorithm
     * @return the generated {@code PublicKey}
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PublicKey toPublicKey(byte[] bytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm.keyFactoryAlgorithm());
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Creates a {@code KeyPair} using the given public and private key byte arrays.
     *
     * @param publicKeyBytes  the byte array representation of the public key
     * @param privateKeyBytes the byte array representation of the private key
     * @param algorithm       the asymmetric algorithm
     * @return a {@code KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        PublicKey publicKey = toPublicKey(publicKeyBytes, algorithm);
        PrivateKey privateKey = toPrivateKey(privateKeyBytes, algorithm);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Generate an Elliptic Curve key pair using a named curve.
     * <p>
     * Supported standard curves:
     * <ul>
     *   <li>"secp256r1" (P-256) - 256-bit curve</li>
     *   <li>"secp384r1" (P-384) - 384-bit curve</li>
     *   <li>"secp521r1" (P-521) - 521-bit curve</li>
     * </ul>
     *
     * @param curveName the name of the elliptic curve ("secp256r1", "secp384r1", "secp521r1")
     * @return the generated EC key pair
     * @throws GeneralSecurityException if key generation fails or the curve is not supported
     */
    public static KeyPair generateECKeyPair(String curveName) throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
        keyGen.initialize(ecSpec, RandomUtil.getRandom());
        return keyGen.generateKeyPair();
    }

    /**
     * Generate an RSA key pair with default key size (2048 bits).
     *
     * @return the generated RSA key pair
     * @throws InvalidAlgorithmParameterException if the key generation fails
     */
    public static KeyPair generateRSAKeyPair() throws InvalidAlgorithmParameterException {
        return generateKeyPair(com.dua3.utility.crypt.AsymmetricAlgorithm.RSA, RSA_DSA_MINIMAL_KEY_SIZE);
    }

    /**
     * Generate an asymmetric key pair.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize   the key size in bits
     * @return the generated key pair
     * @throws InvalidAlgorithmParameterException if key size validation fails or key generation fails
     */
    public static KeyPair generateKeyPair(AsymmetricAlgorithm algorithm, int keySize) throws InvalidAlgorithmParameterException {
        validateAsymmetricKeySize(algorithm, keySize);

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm.keyFactoryAlgorithm());
            keyGen.initialize(keySize, RandomUtil.getRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Validates the size of an asymmetric key based on the specified algorithm.
     * Ensures that the key size meets the minimum or required standards for security purposes.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize   the size of the key in bits, which will be checked against the requirements
     *                  specified for the given algorithm.
     *                  For RSA and DSA, the minimum is 2048 bits.
     *                  For EC, only specific sizes such as 256 (P-256), 384 (P-384), or 521 (P-521) bits are allowed.
     *                  If the key size does not satisfy the constraints for the selected algorithm,
     *                  an exception will be thrown.
     * @throws InvalidAlgorithmParameterException if the key size does not satisfy the constraints
     *                                            for the selected algorithm
     */
    public static void validateAsymmetricKeySize(AsymmetricAlgorithm algorithm, int keySize) throws InvalidAlgorithmParameterException {
        switch (algorithm) {
            case RSA, DSA -> {
                if (keySize < RSA_DSA_MINIMAL_KEY_SIZE) {
                    throw new InvalidAlgorithmParameterException(algorithm + " key size must be at least " + RSA_DSA_MINIMAL_KEY_SIZE + " bits, but was: " + keySize);
                }
            }
            case EC -> {
                if (!EC_ALLOWED_KEY_SIZES.contains(keySize)) {
                    throw new InvalidAlgorithmParameterException("EC key size must be one of " + EC_ALLOWED_KEY_SIZES + ", but was: " + keySize);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + algorithm);
        }
    }

    /**
     * Derives a secret key for the specified symmetric algorithm using a randomly generated salt.
     *
     * @param algorithm           the symmetric algorithm for which the key is to be derived
     * @param input               the input data used for key derivation
     * @param info                optional context-specific information used in the key derivation process
     * @param inputBufferHandling the handling mechanism for input buffers during key derivation
     * @return the derived secret key
     */
    public static SecretKey deriveSecretKeyWithRandomSalt(SymmetricAlgorithm algorithm, byte[] input, byte @Nullable [] info, InputBufferHandling inputBufferHandling) {
        byte[] salt = RandomUtil.generateRandomBytes(32); // 256-bit salt
        return deriveSecretKey(algorithm, salt, input, info, inputBufferHandling);
    }

    /**
     * Derives a cryptographic key using the specified symmetric algorithm, salt, input data, and additional info.
     * The method utilizes the HKDF (HMAC-based Extract-and-Expand Key Derivation Function) with SHA-256 to
     * securely derive a key.
     *
     * @param algorithm           the symmetric algorithm for which the key is being derived; it determines the key size
     *                            and the key algorithm (e.g., AES)
     * @param salt                a non-secret random value used to ensure uniqueness of derived keys; must be at least 16 bytes
     * @param input               the input keying material (IKM) used as a source of entropy for key derivation
     * @param info                optional context and application-specific information used for domain separation during
     *                            key derivation
     * @param inputBufferHandling the handling mechanism for input buffers during key derivation
     * @return a {@link SecretKey} instance containing the derived key that is compatible with the specified algorithm
     * @throws IllegalArgumentException if the provided salt is shorter than 16 bytes
     */
    public static SecretKey deriveSecretKey(SymmetricAlgorithm algorithm, byte[] salt, byte[] input, byte @Nullable [] info, InputBufferHandling inputBufferHandling) {
        try {
            // Validate salt size
            if (salt.length < 16) {
                throw new IllegalArgumentException("Salt must be at least 16 bytes for security");
            }

            // Create HKDF with SHA-256
            HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

            // Initialize HKDF with info parameter for domain separation
            hkdf.init(new HKDFParameters(input, salt, info));

            // Generate key bytes
            byte[] keyBytes = new byte[algorithm.getDefaultKeySize() / 8];
            hkdf.generateBytes(keyBytes, 0, keyBytes.length);

            // Create SecretKey from derived bytes
            return new SecretKeySpec(keyBytes, algorithm.getKeyAlgorithm());
        } finally {
            if (inputBufferHandling != com.dua3.utility.crypt.InputBufferHandling.PRESERVE) {
                Arrays.fill(input, (byte) 0);
                Arrays.fill(salt, (byte) 0);
                if (info != null) {
                    Arrays.fill(info, (byte) 0);
                }
            }
        }
    }

    /**
     * Converts the given cryptographic key to its PEM (Privacy-Enhanced Mail) format.
     *
     * @param key the cryptographic key to be converted into PEM format
     * @return the PEM-encoded string representation of the key
     * @throws UncheckedIOException if an unexpected I/O exception occurs
     */
    public static String toPem(Key key) {
        try {
            StringBuilder sb = new StringBuilder();
            appendPem(key, sb);
            return sb.toString();
        } catch (IOException e) {
            // should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Converts the given cryptographic key to an encrypted PEM (Privacy-Enhanced Mail) format.
     *
     * @param key the cryptographic key to be converted into PEM format
     * @param password the password protecting the key
     * @return the PEM-encoded string representation of the key
     * @throws UncheckedIOException if an unexpected I/O exception occurs
     */
    public static String toPem(Key key, char[] password) {
        try {
            StringBuilder sb = new StringBuilder();
            appendPem(key, password, sb);
            return sb.toString();
        } catch (IOException e) {
            // should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Appends a PEM (Privacy Enhanced Mail) format representation of the specified cryptographic key
     * to the provided {@link Appendable}.
     * <p>
     * The method determines the key type (private or public), extracts its binary encoding, converts
     * it to a Base64-encoded string, and wraps it in the appropriate PEM format with headers and footers.
     *
     * @param key the cryptographic key to be formatted into PEM; must be either a {@link PrivateKey} or {@link PublicKey}.
     * @param app the target {@link Appendable} to which the PEM representation will be appended.
     * @throws IOException if an I/O error occurs while writing to the {@link Appendable}.
     * @throws IllegalStateException if the key type is not supported.
     * @throws IllegalArgumentException if the provided key cannot be encoded.
     */
    public static void appendPem(Key key, Appendable app) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(IoUtil.getWriter(app))) {
            switch (key) {
                case PrivateKey privKey -> pemWriter.writeObject(privKey);
                case PublicKey pubKey -> pemWriter.writeObject(pubKey);
                default -> throw new IllegalStateException("Unsupported key type: " + key.getClass().getName());
            }
        }
    }

    /**
     * Appends the PEM-encoded representation of the given {@link KeyPair} to the specified {@link Appendable}.
     * If the {@link KeyPair} contains both a private and a public key, both are appended.
     * A newline character is appended after each key.
     *
     * @param keyPair the {@link KeyPair} containing the private and/or public key to be encoded and appended
     * @param app the {@link Appendable} to which the PEM-encoded keys will be appended
     * @throws IOException if an I/O error occurs while appending the keys
     * @throws IllegalArgumentException if the {@link KeyPair} does not contain at least one key
     */
    public static void appendPem(KeyPair keyPair, Appendable app) throws IOException {
        appendPem(keyPair, "".toCharArray(), app);
    }

    /**
     * Appends the PEM-encoded representation of a key pair to the specified {@code Appendable}.
     * The private key can be optionally encrypted with a password. The method writes both the
     * public and private keys in PEM format.
     *
     * @param keyPair the {@code KeyPair} containing both the public and private keys.
     *                Must not be null and must contain both keys.
     * @param password a character array representing the password for encrypting the private key.
     *                 If the password is empty (i.e., {@code password.length == 0}), the private
     *                 key will not be encrypted.
     * @param app an {@code Appendable} to which the PEM data will be written.
     *            Must not be null.
     * @throws IOException if an I/O error occurs during writing.
     * @throws IllegalArgumentException if the {@code KeyPair} does not contain both private and public keys.
     */

    public static void appendPem(KeyPair keyPair, char[] password, Appendable app) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        if (privateKey == null || publicKey == null) {
            throw new IllegalArgumentException("KeyPair must contain both private and public keys");
        }

        if (password.length == 0) {
            // Write unencrypted KeyPair - JcaPEMWriter can handle KeyPair directly
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(IoUtil.getWriter(app))) {
                pemWriter.writeObject(keyPair);
            }
        } else {
            // Write encrypted PKCS#8 private key
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(IoUtil.getWriter(app))) {
                OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(
                        PKCS8Generator.AES_256_CBC
                ).setPassword(password).build();

                JcaPKCS8Generator gen = new JcaPKCS8Generator(privateKey, encryptor);
                pemWriter.writeObject(gen);
                pemWriter.writeObject(publicKey);
            } catch (OperatorCreationException e) {
                throw new IOException("Failed to create PEM encryptor", e);
            }
        }

        java.util.Arrays.fill(password, '\0');
    }

    /**
     * Appends the PEM-encoded representation of the private and/or public key from the given {@code KeyPair}
     * to the provided {@code Appendable}. If a password is provided, it is used to encrypt the private key.
     * The public key, if present, is appended in an unencrypted format.
     *
     * @param keyPair the {@code KeyPair} containing the private and/or public key to encode
     * @param app the {@code Appendable} instance to which the encoded keys are appended
     * @param password the password to encrypt the private key, or {@code null} if unencrypted
     * @throws IOException if an I/O error occurs during appending
     * @throws IllegalArgumentException if the provided {@code KeyPair} contains neither a private nor a public key
     */
    public static void appendPem(KeyPair keyPair, Appendable app, char[] password) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        if (privateKey == null && publicKey == null) {
            throw new IllegalArgumentException("KeyPair must contain at least one key");
        }

        // Write encrypted private key (if present) using the existing appendPem(key, password, app),
        // and write public key using JcaPEMWriter so we keep consistent output.
        if (privateKey != null) {
            appendPem(privateKey, password, app); // this method already uses JcaPKCS8Generator
            // ensure separation between entries
            app.append('\n');
        }

        if (publicKey != null) {
            // write public key unencrypted (SubjectPublicKeyInfo)
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(IoUtil.getWriter(app))) {
                try {
                    SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
                    pemWriter.writeObject(spki);
                } catch (Exception e) {
                    pemWriter.writeObject(publicKey);
                }
            }
            app.append('\n');
        }
    }

    /**
     * Appends the PEM-encoded representation of the given key into the provided Appendable instance.
     * If a password is provided, the key will be encrypted using the given password.
     *
     * @param key      the cryptographic key to be encoded
     * @param password the password used to encrypt the key. If null, the key will not be encrypted
     * @param app      the Appendable instance that will receive the PEM-encoded key
     * @throws IOException if an I/O error occurs while appending the data
     */
    public static void appendPem(Key key, char[] password, Appendable app) throws IOException {
        if (password.length == 0) {
            appendPem(key, app); // write unencrypted PEM
            return;
        }

        try (JcaPEMWriter pemWriter = new JcaPEMWriter(IoUtil.getWriter(app))) {
            switch (key) {
                case PrivateKey privKey -> {
                    // Build PKCS#8 encryptor (AES-256-CBC)
                    OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(
                            PKCS8Generator.AES_256_CBC
                    ).setPassword(password).build();

                    // Wrap the PrivateKey in JcaPKCS8Generator and write PEM
                    JcaPKCS8Generator gen = new JcaPKCS8Generator(privKey, encryptor);
                    pemWriter.writeObject(gen);
                }
                case PublicKey pubKey -> pemWriter.writeObject(pubKey);
                default -> throw new IllegalStateException("Unsupported key type: " + key.getClass().getName());
            }
        } catch (OperatorCreationException e) {
            throw new IOException("data could not be written to PEM", e);
        } finally {
            // clear password
            java.util.Arrays.fill(password, '\0');
        }
    }

    /**
     * Parses a DER-encoded key and returns the corresponding key object, either a public or private key,
     * based on the provided byte array.
     *
     * @param bytes the byte array containing the DER-encoded key
     * @return the parsed key object, either a {@link PublicKey} or {@link PrivateKey}
     * @throws GeneralSecurityException if there is an issue with generating the key (e.g., unsupported algorithm)
     */
    public static Key parseDer(byte[] bytes) throws GeneralSecurityException {
        ASN1Sequence asn1Sequence;
        try {
            if (!(ASN1Primitive.fromByteArray(bytes) instanceof ASN1Sequence asn1)) {
                throw new InvalidKeyException("Invalid DER: expected ASN.1 sequence");
            }
            asn1Sequence = asn1;
        } catch (IOException e) {
            // should not happen, all data is in memory
            throw new IllegalStateException("could not parse ASN1 primitive", e);
        }

        if (asn1Sequence.size() == 2 && asn1Sequence.getObjectAt(1) instanceof DERBitString) {
            // Public key
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(asn1Sequence);
            AsymmetricAlgorithm algorithm = getAlgorithmFromOid(spki.getAlgorithm().getAlgorithm().getId());
            return KeyFactory
                    .getInstance(algorithm.keyFactoryAlgorithm())
                    .generatePublic(new X509EncodedKeySpec(bytes));
        } else if (asn1Sequence.size() >= 3 && asn1Sequence.getObjectAt(0) instanceof ASN1Integer
                && asn1Sequence.getObjectAt(1) instanceof ASN1Sequence
                && asn1Sequence.getObjectAt(2) instanceof ASN1OctetString) {
            // Private key
            PrivateKeyInfo pki = PrivateKeyInfo.getInstance(asn1Sequence);
            AsymmetricAlgorithm algorithm = getAlgorithmFromOid(pki.getPrivateKeyAlgorithm().getAlgorithm().getId());
            return KeyFactory
                    .getInstance(algorithm.keyFactoryAlgorithm())
                    .generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } else {
            throw new InvalidKeyException("Unknown DER key format");
        }
    }

    /**
     * Retrieves the corresponding asymmetric algorithm based on the provided OID.
     *
     * @param oid the object identifier (OID) that specifies the asymmetric algorithm
     * @return the {@link AsymmetricAlgorithm} associated with the specified OID
     * @throws InvalidKeyException if the provided OID does not correspond to a known algorithm
     */
    private static AsymmetricAlgorithm getAlgorithmFromOid(String oid) throws InvalidKeyException {
        return switch (oid) {
            case "1.2.840.113549.1.1.1" -> AsymmetricAlgorithm.RSA;
            case "1.2.840.10045.2.1" -> AsymmetricAlgorithm.EC;
            case "1.2.840.10040.4.1" -> AsymmetricAlgorithm.DSA;
            default -> throw new InvalidKeyException("Unknown public key algorithm OID: " + oid);
        };
    }

    /**
     * Converts a given {@link Key} to its encoded form using DER (Distinguished Encoding Rules).
     * Supports X.509 encoding for public keys, PKCS#8 encoding for private keys,
     * and raw byte encoding for symmetric keys.
     *
     * @param key the key to be converted to DER encoding. This can be an instance of
     *            {@link PublicKey}, {@link PrivateKey}, or {@link SecretKey}.
     * @return the DER-encoded byte array representation of the specified key.
     * @throws GeneralSecurityException if the key type is unsupported or an encoding error occurs.
     */
    public static byte[] toDer(Key key) throws GeneralSecurityException {
        return switch (key) {
            // X.509 DER encoding for public keys
            case PublicKey publicKey -> new X509EncodedKeySpec(publicKey.getEncoded()).getEncoded();
            // PKCS#8 DER encoding for private keys
            case PrivateKey privateKey -> new PKCS8EncodedKeySpec(privateKey.getEncoded()).getEncoded();
            // raw bytes for symmetric keys
            case SecretKey secretKey -> secretKey.getEncoded();
            default -> throw new InvalidKeyException("Unsupported key type: " + key.getClass());
        };
    }
}
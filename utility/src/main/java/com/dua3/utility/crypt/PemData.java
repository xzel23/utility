package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The PemData class is used to parse, manage, and retrieve PEM-formatted data.
 * It provides functionality to handle various PEM types such as certificates,
 * private keys, public keys, and PKCS7 objects.
 * The class also supports extracting and handling specific types of data like
 * certificate chains, public keys, or private keys from the parsed PEM data.
 * <p>
 * This class implements the {@link Iterable} interface to support iteration over
 * its parsed components, which are instances of the nested {@link PemItem} class.
 */
public class PemData implements Iterable<PemData.PemItem> {

    private static final Logger LOG = LogManager.getLogger(PemData.class);

    /**
     * Exception class representing errors related to PEM (Privacy-Enhanced Mail) data processing.
     */
    public static class PemException extends Exception {
        /**
         * Constructs a new PemException with the specified detail message.
         *
         * @param message the detail message explaining the reason for the exception
         */
        public PemException(String message) {
            super(message);
        }

        /**
         * Constructs a new PemException with the specified cause.
         *
         * @param cause the cause of the exception, which can be retrieved later 
         *              using the {@link Throwable#getCause()} method. A null 
         *              value is permitted and indicates that the cause is nonexistent 
         *              or unknown.
         */
        public PemException(Throwable cause) {
            super(cause);
        }

        /**
         * Constructs a new PemException with the specified detail message and cause.
         *
         * @param message the detail message, which provides more information about the exception.
         * @param cause the cause of the exception, which can be used to retrieve the original exception that caused this one.
         */
        public PemException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final List<PemItem> items = new ArrayList<>();

    @Override
    public Iterator<PemItem> iterator() {
        return items.iterator();
    }

    /**
     * Enumeration of PEM (Privacy-Enhanced Mail) types commonly encountered 
     * in cryptographic operations. This type classification is used to categorize 
     * and identify the kind of PEM-encoded data parsed or processed.
     */
    public enum PemType {
        /**
         * Unrecognized or unsupported PEM type
         */
        UNKNOWN,
        /**
         * X.509 certificate
         */
        CERTIFICATE,
        /**
         * Chain of X.509 certificates
         */
        CERTIFICATE_CHAIN,
        /**
         * Certificate signing request (CSR)
         */
        CERTIFICATE_REQUEST,
        /**
         * Unencrypted private key
         */
        PRIVATE_KEY,
        /**
         * Encrypted key pair containing both public and private keys
         */
        ENCRYPTED_KEY_PAIR,
        /**
         * Encrypted private key
         */
        ENCRYPTED_PRIVATE_KEY,
        /**
         * Public key
         */
        PUBLIC_KEY,
        /**
         * Unencrypted key pair containing both public and private keys
         */
        KEY_PAIR,
        /**
         * PKCS#7 cryptographic message syntax data
         */
        PKCS7
    }

    /**
     * Represents a single PEM (Privacy-Enhanced Mail) item, consisting of a specific type
     * and its corresponding content. PEM items can include various cryptographic structures
     * such as certificates, private keys, public keys, and certificate chains.
     * <p>
     * This record is composed of:
     * - A {@link PemType} indicating the type of PEM data.
     * - The associated content, represented as an {@link Object}.
     *
     * @param type the type of the PEM item, defined by the {@link PemType} enumeration
     * @param content the content associated with the PEM item, often cryptographic in nature
     */
    public record PemItem(PemType type, Object content) {}

    /**
     * Parses a PEM formatted string and converts it into a {@code PemData} object.
     *
     * @param pem the PEM string to be parsed. It is expected to contain PEM-encoded data.
     * @return a {@code PemData} object containing the parsed contents of the provided PEM string.
     * @throws PemException if an error occurs while parsing the PEM string.
     */
    public static PemData parse(String pem) throws PemException {
        try (Reader reader = new StringReader(pem)) {
            return load(reader);
        } catch (IOException e) {
            // should never happen when reading from String
            throw new PemException(e);
        }
    }

    /**
     * Loads PEM data from the specified file path into a {@code PemData} object.
     *
     * @param path the {@code Path} to the PEM file to be loaded
     * @return a {@code PemData} object containing the parsed PEM data
     * @throws IOException if an I/O error occurs while reading the file
     * @throws PemException if the data in the file is invalid or cannot be parsed
     */
    public static PemData load(Path path) throws IOException, PemException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return load(reader);
        }
    }

    /**
     * Loads PEM (Privacy-Enhanced Mail) data from an {@link InputStream}.
     * This method converts the input stream to a reader with UTF-8 encoding
     * and delegates the actual loading process to another method.
     *
     * @param in the input stream from which PEM data will be read
     * @return a {@link PemData} object containing the parsed PEM data
     * @throws IOException if an I/O error occurs while reading the input stream
     * @throws PemException if there is an error in processing the PEM data
     */
    public static PemData load(InputStream in) throws IOException, PemException {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    /**
     * Loads PEM (Privacy-Enhanced Mail) data from a {@link Reader} and returns a {@code PemData} object.
     * This method processes the input data, extracts various PEM components, and organizes them
     * into a structured {@code PemData} object.
     *
     * @param reader the {@link Reader} instance from which PEM data will be read
     * @return a {@link PemData} object containing the parsed PEM structures
     * @throws IOException if an I/O error occurs during reading
     * @throws PemException if there is an error processing the PEM data
     */
    public static PemData load(Reader reader) throws IOException, PemException {
        PemData result = new PemData();

        List<X509Certificate> certsBuffer = new ArrayList<>();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

        try (PEMParser parser = new PEMParser(reader)) {
            Object obj;
            while ((obj = parser.readObject()) != null) {
                PemItem pemItem = switch (obj) {
                    case X509CertificateHolder holder -> {
                        LOG.debug("Found X509CertificateHolder object");
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                                new ByteArrayInputStream(holder.getEncoded()));
                        certsBuffer.add(cert);
                        yield null; // we defer adding certs until the end
                    }
                    case PKCS10CertificationRequest csr -> {
                        LOG.debug("Found CSR object");
                        yield new PemItem(PemType.CERTIFICATE_REQUEST, csr);
                    }
                    case PEMKeyPair kp when kp.getPublicKeyInfo() == null -> {
                        LOG.debug("Found KeyPair object with only private key");
                        PrivateKey key = KeyUtil.toPrivateKey(kp.getPrivateKeyInfo());
                        yield new PemItem(PemType.PRIVATE_KEY, key);
                    }
                    case PEMKeyPair kp -> {
                        LOG.debug("Found KeyPair object");
                        KeyPair keyPair = converter.getKeyPair(kp);
                        yield new PemItem(PemType.KEY_PAIR, keyPair);
                    }
                    case PEMEncryptedKeyPair encKP -> {
                        LOG.debug("Found encrypted KeyPair object");
                        yield new PemItem(PemType.ENCRYPTED_KEY_PAIR, encKP);
                    }
                    case PrivateKeyInfo info -> {
                        LOG.debug("Found PrivateKeyInfo object");
                        PrivateKey pk = converter.getPrivateKey(info);
                        yield new PemItem(PemType.PRIVATE_KEY, pk);
                    }
                    case PKCS8EncryptedPrivateKeyInfo enc -> {
                        LOG.debug("Found encrypted PrivateKeyInfo object");
                        yield new PemItem(PemType.ENCRYPTED_PRIVATE_KEY, enc);
                    }
                    case org.bouncycastle.asn1.x509.SubjectPublicKeyInfo pubInfo -> {
                        LOG.debug("Found SubjectPublicKeyInfo object");
                        PublicKey pub = converter.getPublicKey(pubInfo);
                        yield new PemItem(PemType.PUBLIC_KEY, pub);
                    }
                    case PemObject po when "PKCS7".equals(po.getType()) -> {
                        LOG.debug("Found PKCS7 object");
                        yield new PemItem(PemType.PKCS7, po.getContent());
                    }
                    default -> {
                        LOG.warn("Unknown PEM object type: {}", obj.getClass().getName());
                        yield new PemItem(PemType.UNKNOWN, obj);
                    }
                };

                if (pemItem != null) {
                    result.addLast(pemItem);
                }
            }
        } catch (GeneralSecurityException e) {
            throw new PemException(e);
        }

        // If we collected certificates, add them as one or multiple PemItem entries
        if (!certsBuffer.isEmpty()) {
            if (certsBuffer.size() == 1) {
                result.addFirst(new PemItem(PemType.CERTIFICATE, certsBuffer.get(0)));
            } else {
                result.addFirst(new PemItem(PemType.CERTIFICATE_CHAIN, List.copyOf(certsBuffer)));
            }
        }

        return result;
    }

    private void addLast(PemItem pemItem) {
        items.addLast(pemItem);
    }

    private void addFirst(PemItem pemItem) {
        items.addFirst(pemItem);
    }

    /**
     * Returns the number of items stored in the current PemData instance.
     *
     * @return the total count of items within the collection.
     */
    public int size() {
        return items.size();
    }

    /**
     * Retrieves the {@code PemItem} at the specified index from the list of PEM items.
     *
     * @param index the index of the item to retrieve, where the index must be greater than or equal to 0
     *              and less than the total number of items in the list.
     * @return the {@code PemItem} located at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public PemItem get(int index) {
        return items.get(index);
    }

    /**
     * Retrieves a list of {@code PemItem} objects filtered by the specified {@code PemType}.
     *
     * @param type the {@code PemType} to filter items by; only items matching this type
     *             will be included in the returned list
     * @return a {@code List} of {@code PemItem} objects that match the specified {@code PemType}
     */
    public List<PemItem> getItems(PemType type) {
        return items.stream().filter(item -> item.type() == type).toList();
    }

    /**
     * Converts the internal collection of PEM items into an unmodifiable list.
     * This method provides a consistent view of the PEM items managed by the containing object,
     * ensuring that the returned list cannot be modified.
     *
     * @return an unmodifiable {@code List} of {@code PemItem} objects representing the PEM data
     */
    public List<PemItem> toList() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Converts the first item in the PEM data collection to an {@code X509Certificate}.
     * This method ensures that the collection contains exactly one item of the type {@code PemType.CERTIFICATE},
     * and casts its content to an {@code X509Certificate}.
     *
     * @return an {@code X509Certificate} object representing the certificate contained in the PEM data.
     * @throws PemException if the collection does not contain exactly one item or if the item type is not {@code PemType.CERTIFICATE}.
     */
    public X509Certificate asCertificate() throws PemException {
        if (size() != 1 || get(0).type() != PemType.CERTIFICATE) {
            throw new PemException("expected a single certificate");
        }
        return (X509Certificate) get(0).content();
    }

    /**
     * Converts the current PemData instance into a list of {@code X509Certificate} objects.
     * Depending on the type of the PEM data, it can either represent a single certificate
     * or a chain of certificates.
     *
     * @return a {@code List} of {@code X509Certificate} objects representing the certificate(s)
     * contained in the PEM data. Returns an empty {@code List} if there are no items.
     * @throws PemException if the PEM data contains multiple unrelated items or if the data type
     * is not a certificate or certificate chain.
     * @throws IllegalStateException if the PEM data contains an unexpected type.
     */
    public List<X509Certificate> asCertificateChain() throws PemException {
        if (size() == 0) {
            return Collections.emptyList();
        }

        if (size() != 1) {
            throw new PemException("PEM data contains multiple items, expected only one");
        }

        return switch (get(0).type()) {
            case CERTIFICATE -> List.of((X509Certificate) get(0).content());
            case CERTIFICATE_CHAIN -> List.copyOf((List<X509Certificate>) (get(0).content()));
            default -> throw new IllegalStateException("expected a single certificate or a certificate chain");
        };
    }

    /**
     * Converts the current PEM data into an array of {@code X509Certificate} objects.
     * This method facilitates retrieval of all certificates in the PEM data as an array.
     *
     * @return an array of {@code X509Certificate} objects representing the certificate(s)
     * contained in the PEM data. The array will be empty if there are no certificates.
     * @throws PemException if the PEM data is invalid, contains unsupported types, or
     * if there are errors during certificate processing.
     */
    public X509Certificate[] asCertificateChainArray() throws PemException {
        return asCertificateChain().toArray(X509Certificate[]::new);
    }

    /**
     * Converts the PEM data contained within this instance into a {@code PublicKey}.
     * This method expects the PEM data to contain exactly one item of type {@code PemType.PUBLIC_KEY}.
     * If the conditions are not met, a {@code PemException} is thrown.
     *
     * @return a {@code PublicKey} object representing the public key contained in the PEM data.
     * @throws PemException if the PEM data does not contain exactly one item,
     *                      or if the item type is not {@code PemType.PUBLIC_KEY}.
     */
    public PublicKey asPublicKey() throws PemException {
        if (size() != 1) {
            throw new IllegalStateException("expected a single entry");
        }
        return toPublicKey(get(0));
    }

    /**
     * Converts the current PEM data into a {@code PrivateKey} object.
     * This method ensures that the PEM data contains exactly one item of type
     * {@code PemType.PRIVATE_KEY} or throws a {@link PemException} if the data does not meet expectations.
     *
     * @return a {@code PrivateKey} object extracted from the PEM data.
     * @throws PemException if the PEM data does not contain exactly one item,
     *                      if the item is not of type {@code PemType.PRIVATE_KEY},
     *                      or if the private key is encrypted.
     */
    public PrivateKey asPrivateKey() throws PemException {
        if (size() != 1) {
            throw new IllegalStateException("expected a single entry");
        }
        return toPrivateKey(get(0));
    }

    /**
     * Converts the PEM data into a {@code PrivateKey} object. This method supports both plain
     * and encrypted private key PEM entries. If the key is encrypted, the provided password
     * will be used to decrypt it. The password array is securely cleared after use.
     *
     * @param password the password used to decrypt the private key, if the key is encrypted.
     *                 This must be a non-null character array.
     * @return the {@code PrivateKey} object represented by the PEM data.
     * @throws PemException if the PEM data does not contain exactly one private key
     *                      or if an error occurs during decryption.
     * @throws IllegalStateException if the PEM data contains multiple entries.
     */
    public PrivateKey asPrivateKey(char[] password) throws PemException {
        try {
            if (size() != 1) {
                throw new IllegalStateException("expected a single entry");
            }
            return toPrivateKey(get(0), password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private PublicKey toPublicKey(PemItem pemItem) throws PemException {
        return switch (pemItem.type()) {
            case PUBLIC_KEY -> (PublicKey) pemItem.content();
            case KEY_PAIR -> ((KeyPair) pemItem.content()).getPublic();
            default -> throw new PemException("expected a single public key");
        };
    }

    private PrivateKey toPrivateKey(PemItem pemItem) throws PemException {
        return switch (pemItem.type()) {
            case PRIVATE_KEY -> (PrivateKey) pemItem.content();
            case KEY_PAIR -> ((KeyPair) pemItem.content()).getPrivate();
            case ENCRYPTED_PRIVATE_KEY ->
                    throw new PemException("private key is encrypted, use asPrivateKey(char[]) instead");
            default -> throw new PemException("unknown type: " + pemItem.type());
        };
    }

    private PrivateKey toPrivateKey(PemItem pemItem, char[] password) throws PemException {
        return switch (pemItem.type()) {
            case PRIVATE_KEY -> (PrivateKey) pemItem.content();
            case ENCRYPTED_PRIVATE_KEY -> decryptKey((PKCS8EncryptedPrivateKeyInfo) pemItem.content(), password);
            case KEY_PAIR -> ((KeyPair) pemItem.content()).getPrivate();
            default -> throw new PemException("expected a single private key, got: " + pemItem.type());
        };
    }

    public KeyPair asKeyPair() throws PemException {
        if (size() != 1 || LangUtil.isNoneOf(get(0).type(), PemType.KEY_PAIR, PemType.ENCRYPTED_KEY_PAIR)) {
            throw new PemException("expected a single key pair");
        }
        if (get(0).type() == PemType.ENCRYPTED_KEY_PAIR) {
            throw new PemException("key pair is encrypted, use asKeyPair(char[]) instead");
        }
        return (KeyPair) get(0).content();
    }

    public KeyPair asKeyPair(char[] password) throws PemException {
        try {
            return switch (size()) {
                case 1 -> switch (get(0).type()) {
                    case KEY_PAIR -> (KeyPair) get(0).content();
                    case ENCRYPTED_KEY_PAIR -> decryptKeyPair((PEMEncryptedKeyPair) get(0).content(), password);
                    default -> throw new PemException("expected a single private key, got: " + get(0).type());
                };
                case 2 -> switch (get(0).type) {
                    case PRIVATE_KEY -> new KeyPair(toPublicKey(get(1)), toPrivateKey(get(0)));
                    case ENCRYPTED_PRIVATE_KEY -> new KeyPair(toPublicKey(get(1)), toPrivateKey(get(0), password));
                    default -> throw new PemException("not a valid key pair");
                };
                default -> throw new PemException("not a key pair");
            };
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Decrypts an encrypted PEM key pair using the provided password and converts
     * it into a {@code KeyPair} object.
     *
     * @param kp the {@code PEMEncryptedKeyPair} to be decrypted
     * @param password the password used to decrypt the key pair
     * @return a {@code KeyPair} object representing the decrypted key pair
     * @throws PemException if the decryption process fails
     */
    private static KeyPair decryptKeyPair(PEMEncryptedKeyPair kp, char[] password) throws PemException {
        try {
            PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password);
            return new JcaPEMKeyConverter().getKeyPair(kp.decryptKeyPair(provider));
        } catch (IOException e) {
            throw new PemException("encrypted keys could not be decrypted", e);
        }
    }

    /**
     * Decrypts a PKCS8 encrypted private key using the provided password.
     *
     * @param enc the {@code PKCS8EncryptedPrivateKeyInfo} containing the encrypted private key
     * @param password the password to use for decrypting the private key
     * @return the decrypted {@code PrivateKey} instance
     * @throws IllegalStateException if the encrypted key cannot be decrypted due to an I/O error,
     *         a PKCS processing error, or an operator creation error
     */
    private static PrivateKey decryptKey(PKCS8EncryptedPrivateKeyInfo enc, char[] password) {
        try {
            var decryptorProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
            var pkInfo = enc.decryptPrivateKeyInfo(decryptorProvider);
            return new JcaPEMKeyConverter().getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new IllegalStateException("encrypted keys could not be decrypted", e);
        }
    }

}

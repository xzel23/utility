package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * KeyStoreType defines a set of constants representing different types of keystores.
 * A keystore is a repository for storing cryptographic keys and certificates securely.
 * Each constant corresponds to a specific keystore format, commonly used in cryptographic
 * operations and secure communication protocols like SSL/TLS.
 * <p>
 * Supported keystore types include:
 * - PKCS12: A popular format for storing cryptographic keys and certificates, supporting strong encryption.
 * - JKS: A Java-specific format for securely managing keys and trusted certificates.
 * - JCEKS: An extension of JKS providing additional support for strong encryption algorithms.
 * <p>
 * These formats enable secure storage and management of cryptographic materials, ensuring
 * integrity and confidentiality, while also supporting interoperability between platforms.
 */
public enum KeyStoreType {
    /**
     * Represents the PKCS12 keystore format, a standard defined by the Public-Key Cryptography Standards (PKCS) #12.
     * PKCS12 is widely used for securely storing cryptographic keys and certificates, enabling interoperability
     * between various cryptographic systems and software. It supports strong encryption and is commonly used
     * in SSL/TLS implementations.
     */
    PKCS12(false, true, "p12"),
    /**
     * Represents the Java KeyStore (JKS) type, a proprietary keystore format used in Java applications
     * for managing cryptographic keys, certificates, and trusted certificate chains.
     * Commonly utilized in Java environments for secure storage and cryptographic operations.
     */
    JKS(false, false, "jks"),
    /**
     * Represents the Java Cryptography Extension KeyStore (JCEKS) format,
     * an extended version of the Java KeyStore (JKS) providing support
     * for stronger encryption algorithms.
     * <p>
     * JCEKS is commonly used when enhanced security features are required
     * for storing cryptographic keys and certificates.
     */
    JCEKS(false, false, "jceks"),
    /**
     * A Zip file containing one file per alias.
     * <p>
     * Use ZIP for exporting KeyStore entries to be used by systems lacking proper KeyStore support (LESS SECURE).
     */
    ZIP(true, false, "zip");

    private final boolean isExportOnly;
    private final boolean isDeduplicating;
    private final String[] extensions;

    KeyStoreType(boolean isExportOnly, boolean isDeduplicating, String... extensions) {
        LangUtil.check(extensions.length > 0, "no extensions provided");
        this.isExportOnly = isExportOnly;
        this.isDeduplicating = isDeduplicating;
        this.extensions = extensions;
    }

    /**
     * Determines the appropriate {@code KeyStoreType} for the provided file extension.
     * This method maps common keystore file extensions to their corresponding {@code KeyStoreType}.
     *
     * @param extension the file extension to be matched to a {@code KeyStoreType}.
     *                  It can optionally start with a dot (e.g., ".jks" or "p12").
     * @return the {@code KeyStoreType} corresponding to the specified file extension.
     * @throws IllegalArgumentException if the provided extension is not supported by any {@code KeyStoreType}.
     */
    public static KeyStoreType forExtension(String extension) {
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }

        for (KeyStoreType type : values()) {
            if (LangUtil.isOneOf(extension, type.extensions)) {
                return type;
            }
        }

        throw new IllegalArgumentException("unsupported keystore extension: " + extension);
    }

    /**
     * Retrieves the (standard) file extension associated with the current {@code KeyStoreType}.
     *
     * @return a string representing the file extension for the keystore type.
     */
    public String getExtension() {
        return extensions[0];
    }

    /**
     * Determines the appropriate {@code KeyStoreType} based on the file path provided.
     * This method extracts the file extension from the provided {@code Path} and maps
     * it to the corresponding {@code KeyStoreType}.
     *
     * @param path the {@code Path} representing the keystore file. The file extension
     *             will be derived from the path to identify the keystore type.
     * @return the {@code KeyStoreType} corresponding to the file extension of the provided path.
     * @throws IllegalArgumentException if the file extension from the path is unsupported
     *                                  by any {@code KeyStoreType}.
     */
    public static KeyStoreType forPath(Path path) {
        return forExtension(IoUtil.getExtension(path));
    }

    /**
     * Determines the appropriate {@code KeyStoreType} for the given file path.
     * This method extracts the file extension from the provided path and maps it
     * to the corresponding {@code KeyStoreType}.
     *
     * @param path the file path from which the keystore type will be determined.
     *             It is expected to be a valid string containing a file extension.
     * @return the {@code KeyStoreType} corresponding to the file extension in the path.
     * @throws IllegalArgumentException if the file extension is not supported by any {@code KeyStoreType}.
     */
    public static KeyStoreType forPath(String path) {
        return forExtension(IoUtil.getExtension(path));
    }

    /**
     * Retrieves an array of {@code KeyStoreType} values that support writing operations.
     *
     * @return an array of {@code KeyStoreType} instances that support writing.
     */
    public static KeyStoreType[] valuesWriteable() {
        return values();
    }

    /**
     * Retrieves an array of {@code KeyStoreType} values that support reading operations.
     *
     * @return an array of {@code KeyStoreType} instances that support reading.
     */
    public static KeyStoreType[] valuesReadble() {
        return Arrays.stream(values()).filter(Predicate.not(KeyStoreType::isExportOnly)).toArray(KeyStoreType[]::new);
    }

    /**
     * Indicates whether reading operations are supported for this {@code KeyStoreType}.
     *
     * @return {@code true} if reading is supported for this keystore type,
     *         {@code false} otherwise.
     */
    public boolean isExportOnly() {
        return isExportOnly;
    }

    /**
     * Indicates whether this {@code KeyStoreType} uses deduplication.
     * <p>
     * PKSC#12 keystores deduplicate certificate chains when saving.
     * This means, entries that are already stored in the keystore will
     * automatically be removed from the parent chains of certificates.
     *
     * @return {@code true} if this keystore type uses deduplication,
     *         {@code false} otherwise.
     */
    public boolean isDeduplicating() {
        return isDeduplicating;
    }
}
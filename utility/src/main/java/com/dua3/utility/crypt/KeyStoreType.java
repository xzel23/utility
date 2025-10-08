package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;

import java.nio.file.Path;
import java.util.Locale;

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
    PKCS12,
    /**
     * Represents the Java KeyStore (JKS) type, a proprietary keystore format used in Java applications
     * for managing cryptographic keys, certificates, and trusted certificate chains.
     * Commonly utilized in Java environments for secure storage and cryptographic operations.
     */
    JKS,
    /**
     * Represents the Java Cryptography Extension KeyStore (JCEKS) format,
     * an extended version of the Java KeyStore (JKS) providing support
     * for stronger encryption algorithms.
     * <p>
     * JCEKS is commonly used when enhanced security features are required
     * for storing cryptographic keys and certificates.
     */
    JCEKS;

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

        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "pfx", "p12" -> PKCS12;
            case "jks" -> JKS;
            case "jceks" -> JCEKS;
            default -> throw new IllegalArgumentException("unsupported keystore extension: " + extension);
        };
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
}
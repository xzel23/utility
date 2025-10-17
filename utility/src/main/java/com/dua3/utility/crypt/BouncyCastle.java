package com.dua3.utility.crypt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;
import java.util.Optional;

/**
 * The {@code BouncyCastleInitializer} class provides functionality to ensure
 * that the BouncyCastle security provider is registered and available in
 * the Java security environment. This class automates the registration
 * of the BouncyCastle provider upon its usage and ensures it is properly
 * initialized before any cryptographic operations relying on this provider
 * can be performed.
 */
public final class BouncyCastle {
    private static final Logger LOG = LogManager.getLogger(BouncyCastle.class);

    static final class ProviderHolder {
        private ProviderHolder() { /* nothing to do */
        }

        // Register BouncyCastle provider if not already registered
        static {
            if (java.security.Security.getProvider("BC") == null) {
                try {
                    Class<?> cls = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                    java.security.Security.addProvider(((org.bouncycastle.jce.provider.BouncyCastleProvider) cls.getConstructor().newInstance()));
                    LOG.debug("BouncyCastle provider registered");
                } catch (ClassNotFoundException e) {
                    LOG.warn("BouncyCastle provider not found on classpath");
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    LOG.warn("BouncyCastle provider could not be registered", e);
                }
            }
        }

        private static final @Nullable Provider PROVIDER = Security.getProvider("BC");
    }

    private BouncyCastle() { /* utility class constructor */ }

    /**
     * Retrieves the BouncyCastle security provider instance. If the provider
     * is not available, an exception will be thrown.
     *
     * @return the registered BouncyCastle security provider
     * @throws IllegalStateException if the provider is not available
     */
    public static Provider ensureProvider() {
        if (ProviderHolder.PROVIDER != null) {
            return ProviderHolder.PROVIDER;
        } else {
            LOG.warn("BouncyCastle provider not available");
            throw new IllegalStateException("BouncyCastle provider not available");
        }
    }

    /**
     * Retrieves the BouncyCastle security provider instance, if available.
     * This method returns an {@link Optional} containing the registered BouncyCastle
     * provider if it is present in the security framework, or an empty {@link Optional}
     * if the provider is not available.
     *
     * @return an {@link Optional} containing the BouncyCastle security provider if available,
     * or an empty {@link Optional} if the provider is not registered.
     */
    @SuppressWarnings("OptionalContainsCollection")
    public static Optional<Provider> getProvider() {
        return Optional.ofNullable(ProviderHolder.PROVIDER);
    }

    /**
     * Ensures that the BouncyCastle security provider is properly initialized and available.
     * This method acts as a guard, ensuring that the security provider has been registered
     * in the system before it is used. If the BouncyCastle provider is not available,
     * an exception will be thrown.
     * <p>
     * This method is used to enforce the presence of the BouncyCastle provider
     * during initialization or setup stages in applications relying on it.
     *
     * @throws IllegalStateException if the provider is not available
     */
    public static void ensureAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("BouncyCastle provider not available");
        }
    }

    /**
     * Checks if the BouncyCastle security provider is available in the system.
     * This method verifies whether the BouncyCastle provider has been successfully
     * registered and is ready for use.
     *
     * @return {@code true} if the BouncyCastle provider is available; {@code false} otherwise
     */
    public static boolean isAvailable() {
        return ProviderHolder.PROVIDER != null;
    }
}

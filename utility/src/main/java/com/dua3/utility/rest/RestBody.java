package com.dua3.utility.rest;

import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A request body that can create a fresh {@link HttpRequest.BodyPublisher} for every attempt.
 *
 * <p>Requests retried after an authentication refresh require a repeatable body. The factory
 * methods in this class create repeatable bodies, except when {@link #ofPublisher(HttpRequest.BodyPublisher,
 * boolean, String)} is told otherwise.</p>
 */
public interface RestBody {

    /**
     * Creates a body publisher for one request attempt.
     *
     * @return a new body publisher
     */
    HttpRequest.BodyPublisher newPublisher();

    /**
     * Indicates whether the body can safely be sent again.
     *
     * @return {@code true} if a retry can recreate the body
     */
    boolean repeatable();

    /**
     * Gets the body's media type, if the body provides one.
     *
     * @return the media type or {@code null}
     */
    @Nullable String contentType();

    /**
     * Creates an empty request body.
     *
     * @return an empty, repeatable body
     */
    static RestBody empty() {
        return ofPublisher(HttpRequest.BodyPublishers.noBody(), true, null);
    }

    /**
     * Creates a UTF-8 text body.
     *
     * @param value the text to send
     * @return a repeatable text body
     */
    static RestBody text(String value) {
        return text(value, "text/plain; charset=utf-8");
    }

    /**
     * Creates a UTF-8 text body with the supplied media type.
     *
     * @param value the text to send
     * @param contentType the media type
     * @return a repeatable text body
     */
    static RestBody text(String value, String contentType) {
        Objects.requireNonNull(value, "value");
        return bytes(value.getBytes(StandardCharsets.UTF_8), contentType);
    }

    /**
     * Creates a byte-array body.
     *
     * @param value the bytes to send
     * @param contentType the media type
     * @return a repeatable body
     */
    static RestBody bytes(byte[] value, String contentType) {
        Objects.requireNonNull(value, "value");
        byte[] copy = value.clone();
        return new SimpleRestBody(() -> HttpRequest.BodyPublishers.ofByteArray(copy), true, contentType);
    }

    /**
     * Creates a streaming body. The supplier must return a fresh stream for every invocation.
     *
     * @param streamSupplier supplier of request streams
     * @param contentType the media type
     * @return a repeatable body
     */
    static RestBody inputStream(Supplier<? extends InputStream> streamSupplier, String contentType) {
        Objects.requireNonNull(streamSupplier, "streamSupplier");
        return new SimpleRestBody(() -> HttpRequest.BodyPublishers.ofInputStream(streamSupplier::get), true, contentType);
    }

    /**
     * Wraps a body publisher.
     *
     * @param publisher the publisher
     * @param repeatable whether the same publisher can safely be used again
     * @param contentType the media type, or {@code null}
     * @return the wrapped body
     */
    static RestBody ofPublisher(HttpRequest.BodyPublisher publisher, boolean repeatable, @Nullable String contentType) {
        Objects.requireNonNull(publisher, "publisher");
        return new SimpleRestBody(() -> publisher, repeatable, contentType);
    }

    /**
     * Internal immutable implementation shared by the factory methods.
     *
     * @param publisherSupplier supplies a publisher for each request attempt
     * @param repeatable whether the body can be sent again
     * @param contentType the media type, or {@code null}
     */
    record SimpleRestBody(Supplier<? extends HttpRequest.BodyPublisher> publisherSupplier,
                          boolean repeatable,
                          @Nullable String contentType) implements RestBody {
        @Override
        public HttpRequest.BodyPublisher newPublisher() {
            return Objects.requireNonNull(publisherSupplier.get(), "publisherSupplier returned null");
        }
    }
}

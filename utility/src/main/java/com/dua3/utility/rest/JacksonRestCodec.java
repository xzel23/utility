package com.dua3.utility.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Reflection-based Jackson 3 adapter, keeping Jackson out of this module's public API. */
final class JacksonRestCodec implements RestCodec {
    private static final Logger LOG = LogManager.getLogger(JacksonRestCodec.class);
    private static final String JSON_MAPPER = "tools.jackson.databind.json.JsonMapper";
    private static final String YAML_MAPPER = "tools.jackson.dataformat.yaml.YAMLMapper";

    private final Object mapper;
    private final Method writeValueAsBytes;
    private final Method readValue;
    private final String contentType;

    private JacksonRestCodec(String mapperClassName, String contentType, String requiredDependency) {
        LOG.debug("Creating Jackson mapper {}", mapperClassName);
        try {
            Class<?> mapperClass = Class.forName(mapperClassName);
            this.mapper = mapperClass.getConstructor().newInstance();
            this.writeValueAsBytes = mapperClass.getMethod("writeValueAsBytes", Object.class);
            this.readValue = mapperClass.getMethod("readValue", byte[].class, Class.class);
            this.contentType = contentType;
        } catch (ClassNotFoundException exception) {
            LOG.error("Jackson mapper class is unavailable: {}", mapperClassName, exception);
            throw unavailable(requiredDependency, exception);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            LOG.error("Jackson mapper {} is incompatible", mapperClassName, exception);
            throw new UnsupportedOperationException(
                    "Jackson 3 is present but does not provide a compatible " + mapperClassName, exception);
        }
    }

    static RestCodec json() {
        return new JacksonRestCodec(JSON_MAPPER, "application/json", "Jackson 3 databind");
    }

    static Map<String, Object> readOpenApi(InputStream input, OpenApiFormat format) throws IOException {
        String mapperClass = format == OpenApiFormat.YAML ? YAML_MAPPER : JSON_MAPPER;
        String mediaType = format == OpenApiFormat.YAML ? "application/yaml" : "application/json";
        String dependency = format == OpenApiFormat.YAML ? "Jackson 3 YAML support" : "Jackson 3 databind";
        JacksonRestCodec codec = new JacksonRestCodec(mapperClass, mediaType, dependency);
        //noinspection OverlyBroadCatchBlock
        try {
            byte[] document = input.readAllBytes();
            Map<?, ?> values = codec.read(document, Map.class);
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("OpenAPI document contains a non-string object key");
                }
                result.put(key, entry.getValue());
            }
            return result;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        } catch (RuntimeException e) {
            throw new IOException("could not parse OpenAPI document as " + mediaType, e);
        }
    }

    @Override
    public RestBody write(Object value) {
        return RestBody.bytes(serialize(value), contentType);
    }

    @Override
    public <T> HttpResponse.BodyHandler<T> bodyHandler(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofByteArray(), bytes -> read(bytes, type));
    }

    @Override
    public String contentType() {
        return contentType;
    }

    private byte[] serialize(Object value) {
        try {
            return (byte[]) writeValueAsBytes.invoke(mapper, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Jackson 3 JSON writer is inaccessible", exception);
        } catch (InvocationTargetException exception) {
            throw rethrow("could not serialize REST request body", exception.getCause());
        }
    }

    private <T> T read(byte[] bytes, Class<T> type) {
        try {
            return type.cast(readValue.invoke(mapper, bytes, type));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Jackson 3 JSON reader is inaccessible", exception);
        } catch (InvocationTargetException exception) {
            throw rethrow("could not deserialize REST response body", exception.getCause());
        }
    }

    private static IllegalArgumentException rethrow(String message, Throwable cause) {
        if (cause instanceof RuntimeException exception) {
            return new IllegalArgumentException(message, exception);
        }
        return new IllegalArgumentException(message, cause);
    }

    private static UnsupportedOperationException unavailable(String dependency, ClassNotFoundException cause) {
        return new UnsupportedOperationException(
                dependency + " is required for this operation; add it as an optional runtime dependency", cause);
    }
}

package com.dua3.utility.rest;

import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Imports the OpenAPI 3 subset represented by {@link RestApi} and {@link RestEndpoint}. */
final class OpenApiImporter {
    private static final Set<String> HTTP_METHODS = Set.of(
            "get", "put", "post", "delete", "options", "head", "patch", "trace");

    private OpenApiImporter() {
        // Utility class.
    }

    static void importInto(RestApiBuilder builder, InputStream input, OpenApiFormat requestedFormat) throws IOException {
        byte[] document = input.readAllBytes();
        OpenApiFormat format = requestedFormat == OpenApiFormat.AUTO ? detectFormat(document) : requestedFormat;
        Map<String, Object> root = JacksonRestCodec.readOpenApi(new ByteArrayInputStream(document), format);

        String version = string(root.get("openapi"));
        if (version == null || !version.startsWith("3.")) {
            throw new IllegalArgumentException("only OpenAPI 3.x documents are supported");
        }

        importServer(builder, root);
        List<Map<String, Object>> globalSecurity = securityRequirements(root.get("security"), root);
        Map<String, Object> paths = object(root.get("paths"), root, "paths");
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            importPath(builder, root, pathEntry.getKey(), object(pathEntry.getValue(), root, "path item"), globalSecurity);
        }
    }

    private static void importServer(RestApiBuilder builder, Map<String, Object> root) {
        if (builder.hasBaseUri()) {
            return;
        }
        List<?> servers = list(root.get("servers"));
        if (servers.isEmpty()) {
            return;
        }
        Map<String, Object> server = object(servers.getFirst(), root, "server");
        String url = string(server.get("url"));
        if (url == null || url.isBlank()) {
            return;
        }
        url = substituteServerVariables(url, objectOrEmpty(server.get("variables"), root));
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        if (uri.isAbsolute() && uri.getHost() != null) {
            builder.baseUriIfUnset(uri);
        }
    }

    private static String substituteServerVariables(String url, Map<String, Object> variables) {
        String result = url;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Map<String, Object> variable = objectOrEmpty(entry.getValue(), variables);
            String defaultValue = string(variable.get("default"));
            if (defaultValue != null) {
                result = result.replace("{" + entry.getKey() + '}', defaultValue);
            }
        }
        return result;
    }

    private static void importPath(RestApiBuilder apiBuilder, Map<String, Object> root, String path,
                                   Map<String, Object> pathItem, List<Map<String, Object>> globalSecurity) {
        List<Map<String, Object>> pathParameters = parameterDefinitions(pathItem.get("parameters"), root);
        for (Map.Entry<String, Object> operationEntry : pathItem.entrySet()) {
            String method = operationEntry.getKey().toLowerCase(Locale.ROOT);
            if (!HTTP_METHODS.contains(method)) {
                continue;
            }
            Map<String, Object> operation = object(operationEntry.getValue(), root, "operation");
            String operationName = string(operation.get("operationId"));
            if (operationName == null || operationName.isBlank()) {
                operationName = generatedOperationName(method, path);
            }

            RestEndpoint.Builder endpoint = RestEndpoint.builder(operationName).method(method, path);
            importParameters(endpoint, mergeParameters(pathParameters, parameterDefinitions(operation.get("parameters"), root)));
            importRequestBody(endpoint, operation.get("requestBody"), root);
            importResponses(endpoint, operation.get("responses"), root);
            List<Map<String, Object>> security = operation.containsKey("security")
                    ? securityRequirements(operation.get("security"), root) : globalSecurity;
            for (Map<String, Object> requirement : security) {
                requirement.keySet().forEach(endpoint::securityRequirement);
            }
            apiBuilder.endpoint(endpoint.build());
        }
    }

    private static void importParameters(RestEndpoint.Builder endpoint, Collection<Map<String, Object>> parameters) {
        for (Map<String, Object> parameter : parameters) {
            String name = string(parameter.get("name"));
            String location = string(parameter.get("in"));
            if (name == null || location == null) {
                throw new IllegalArgumentException("OpenAPI parameter requires 'name' and 'in'");
            }
            RestParameterLocation parameterLocation = switch (location) {
                case "path" -> RestParameterLocation.PATH;
                case "query" -> RestParameterLocation.QUERY;
                case "header" -> RestParameterLocation.HEADER;
                case "cookie" -> RestParameterLocation.COOKIE;
                default -> throw new IllegalArgumentException("unsupported OpenAPI parameter location: " + location);
            };
            boolean required = Boolean.TRUE.equals(parameter.get("required"));
            endpoint.parameter(name, parameterLocation, required);
        }
    }

    private static void importRequestBody(RestEndpoint.Builder endpoint, Object value, Map<String, Object> root) {
        Map<String, Object> requestBody = object(value, root, "request body");
        endpoint.requestBodyRequired(Boolean.TRUE.equals(requestBody.get("required")));
        objectOrEmpty(requestBody.get("content"), root).keySet().forEach(endpoint::consumes);
    }

    private static void importResponses(RestEndpoint.Builder endpoint, Object value, Map<String, Object> root) {
        for (Map.Entry<String, Object> response : object(value, root, "responses").entrySet()) {
            try {
                endpoint.expectedStatus(Integer.parseInt(response.getKey()));
            } catch (NumberFormatException ignored) {
                // OpenAPI also permits "default" and ranges such as "2XX".
            }
            Map<String, Object> responseDefinition = object(response.getValue(), root, "response");
            objectOrEmpty(responseDefinition.get("content"), root).keySet().forEach(endpoint::produces);
        }
    }

    private static List<Map<String, Object>> parameterDefinitions(Object value, Map<String, Object> root) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object parameter : list(value)) {
            result.add(object(parameter, root, "parameter"));
        }
        return result;
    }

    private static Collection<Map<String, Object>> mergeParameters(List<Map<String, Object>> pathParameters,
                                                                   List<Map<String, Object>> operationParameters) {
        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> parameter : pathParameters) {
            merged.put(parameterKey(parameter), parameter);
        }
        for (Map<String, Object> parameter : operationParameters) {
            merged.put(parameterKey(parameter), parameter);
        }
        return merged.values();
    }

    private static String parameterKey(Map<String, Object> parameter) {
        String name = string(parameter.get("name"));
        String location = string(parameter.get("in"));
        if (name == null || location == null) {
            throw new IllegalArgumentException("OpenAPI parameter requires 'name' and 'in'");
        }
        return location + '\u0000' + name;
    }

    private static List<Map<String, Object>> securityRequirements(Object value, Map<String, Object> root) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object requirement : list(value)) {
            result.add(object(requirement, root, "security requirement"));
        }
        return result;
    }

    private static Map<String, Object> object(Object value, Map<String, Object> root, String description) {
        Object resolved = resolve(value, root);
        if (!(resolved instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("OpenAPI " + description + " must be an object");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("OpenAPI " + description + " has a non-string key");
            }
            result.put(key, entry.getValue());
        }
        return result;
    }

    private static Map<String, Object> objectOrEmpty(@Nullable Object value, Map<String, Object> root) {
        return value == null ? Map.of() : object(value, root, "object");
    }

    private static Object resolve(Object value, Map<String, Object> root) {
        if (!(value instanceof Map<?, ?> map)) {
            return value;
        }
        Object reference = map.get("$ref");
        if (!(reference instanceof String ref)) {
            return value;
        }
        if (!ref.startsWith("#/")) {
            throw new IllegalArgumentException("external OpenAPI references are not supported: " + ref);
        }
        Object current = root;
        for (String rawPart : ref.substring(2).split("/")) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                throw new IllegalArgumentException("invalid OpenAPI reference: " + ref);
            }
            current = currentMap.get(rawPart.replace("~1", "/").replace("~0", "~"));
            if (current == null) {
                throw new IllegalArgumentException("OpenAPI reference does not exist: " + ref);
            }
        }
        return current;
    }

    private static List<?> list(@Nullable Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> result)) {
            throw new IllegalArgumentException("OpenAPI value must be an array");
        }
        return result;
    }

    private static String generatedOperationName(String method, String path) {
        StringBuilder name = new StringBuilder(method.toLowerCase(Locale.ROOT));
        for (String segment : path.split("/")) {
            if (!segment.isBlank()) {
                String normalized = segment.replaceAll("[^A-Za-z0-9]+", " ").trim();
                for (String word : normalized.split("\\s+")) {
                    if (!word.isEmpty()) {
                        name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                    }
                }
            }
        }
        return name.toString();
    }

    private static OpenApiFormat detectFormat(byte[] document) {
        String text = new String(document, StandardCharsets.UTF_8);
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character != '\ufeff' && !Character.isWhitespace(character)) {
                return character == '{' ? OpenApiFormat.JSON : OpenApiFormat.YAML;
            }
        }
        throw new IllegalArgumentException("OpenAPI document is empty");
    }

    private static @Nullable String string(Object value) {
        return value instanceof String s ? s : null;
    }
}

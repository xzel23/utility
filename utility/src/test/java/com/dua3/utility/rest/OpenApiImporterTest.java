package com.dua3.utility.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiImporterTest {

    @TempDir
    Path tempDir;

    @Test
    void importsReferencedDefinitionsParametersContentAndSecurity() throws Exception {
        String document = """
                {
                  "openapi": "3.1.1",
                  "servers": [{
                    "url": "https://{environment}.example.test/{basePath}",
                    "variables": {
                      "environment": {"default": "api"},
                      "basePath": {"default": "v2"}
                    }
                  }],
                  "security": [{"oauth": []}],
                  "components": {
                    "parameters": {
                      "PetId": {"name": "petId", "in": "path", "required": true},
                      "Limit": {"name": "limit", "in": "query", "required": false}
                    },
                    "requestBodies": {
                      "Pet": {"required": true, "content": {
                        "application/json": {}, "application/xml": {}
                      }}
                    },
                    "responses": {
                      "Pet": {"description": "OK", "content": {"application/json": {}}}
                    }
                  },
                  "paths": {
                    "/pets/{petId}": {
                      "parameters": [
                        {"$ref": "#/components/parameters/PetId"},
                        {"$ref": "#/components/parameters/Limit"}
                      ],
                      "post": {
                        "operationId": "replacePet",
                        "parameters": [
                          {"name": "limit", "in": "query", "required": true},
                          {"name": "X-Trace", "in": "header", "required": true},
                          {"name": "session", "in": "cookie", "required": false}
                        ],
                        "requestBody": {"$ref": "#/components/requestBodies/Pet"},
                        "responses": {
                          "200": {"$ref": "#/components/responses/Pet"},
                          "202": {"description": "Accepted", "content": {"application/xml": {}}},
                          "default": {"description": "Fallback"}
                        },
                        "security": [{"apiKey": []}]
                      }
                    }
                  }
                }
                """;

        RestApi api = RestApi.builder()
                .fromOpenApi(input(document), OpenApiFormat.AUTO)
                .build();
        RestEndpoint endpoint = api.endpoint("replacePet").orElseThrow();

        assertEquals("https://api.example.test/v2/", api.baseUri().toString());
        assertEquals("POST", endpoint.method());
        assertEquals("/pets/{petId}", endpoint.pathTemplate());
        assertEquals(List.of("petId", "limit", "X-Trace", "session"),
                endpoint.parameters().stream().map(RestParameter::name).toList());
        assertTrue(endpoint.parameter("limit", RestParameterLocation.QUERY).orElseThrow().required());
        assertTrue(endpoint.parameter("x-trace", RestParameterLocation.HEADER).orElseThrow().required());
        assertTrue(endpoint.requestBodyRequired());
        assertEquals(Set.of("application/json", "application/xml"), endpoint.requestContentTypes());
        assertEquals(Set.of("application/json", "application/xml"), endpoint.responseContentTypes());
        assertEquals(Set.of(200, 202), endpoint.expectedStatuses());
        assertEquals(Set.of("apiKey"), endpoint.securityRequirements());
        assertFalse(endpoint.retryAfterAuthenticationRefresh());
    }

    @Test
    void generatesOperationNamesAndRejectsUnsupportedVersions() throws Exception {
        String document = """
                {"openapi":"3.0.3","servers":[{"url":"https://example.test"}],"paths":{
                  "/pets/{pet-id}": {"parameters":[{"name":"pet-id","in":"path","required":true}],
                    "get":{"responses":{"204":{"description":"empty"}}}}
                }}
                """;
        RestApi api = RestApi.builder().fromOpenApi(input(document)).build();
        assertTrue(api.endpoint("getPetsPetId").isPresent());

        String versionTwo = """
                {"swagger":"2.0","paths":{}}
                """;
        assertThrows(IllegalArgumentException.class,
                () -> RestApi.builder().fromOpenApi(input(versionTwo), OpenApiFormat.JSON));
    }

    @Test
    void reportsMissingOptionalYamlSupport() {
        String document = """
                openapi: 3.0.3
                servers:
                  - url: https://example.test
                paths: {}
                """;
        assertThrows(UnsupportedOperationException.class,
                () -> RestApi.builder().fromOpenApi(input(document), OpenApiFormat.YAML));
    }

    @Test
    void importsJsonFilesUsingTheirExtension() throws Exception {
        Path document = tempDir.resolve("openapi.json");
        Files.writeString(document, """
                {"openapi":"3.0.3","servers":[{"url":"https://example.test"}],"paths":{
                  "/health":{"get":{"operationId":"health","responses":{"200":{"description":"OK"}}}}
                }}
                """);

        RestApi api = RestApi.builder().fromOpenApi(document).build();
        assertTrue(api.endpoint("health").isPresent());
    }

    private static ByteArrayInputStream input(String document) {
        return new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));
    }
}

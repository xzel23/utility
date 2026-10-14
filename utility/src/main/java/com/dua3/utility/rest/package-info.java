/**
 * A lightweight, declarative REST client built on Java's {@link java.net.http.HttpClient}.
 *
 * <h2>Requirements</h2>
 *
 * The core REST client requires Java's {@code java.net.http} module.
 * Authentication, request construction, and response handling do not require
 * any JSON library.
 * <p>
 * OpenAPI import and JSON mapping are optional and use Jackson 3. For JSON, add the
 * {@code tools.jackson.core}, {@code tools.jackson.databind}, and transitive
 * {@code com.fasterxml.jackson.annotation} modules. YAML import additionally requires
 * {@code tools.jackson.dataformat.yaml} and {@code org.snakeyaml.engine}. When using the
 * Java module path, the application module must resolve these modules explicitly, for example:
 *
 * <pre>{@code
 * requires tools.jackson.databind;
 * requires tools.jackson.dataformat.yaml;
 * }</pre>
 *
 * <p>
 * If the optional Jackson modules are unavailable, JSON mapping and OpenAPI import throw
 * {@link UnsupportedOperationException}.
 *
 * <h2>Defining an API without OpenAPI</h2>
 *
 * Endpoints can be declared directly with the builder. Parameters are validated when a
 * request is executed.
 *
 * <pre>{@code
 * RestApi api = RestApi.builder()
 *         .baseUri("https://api.example.com/v1")
 *         .endpoint("getPet", endpoint -> endpoint
 *                 .get("/pets/{id}")
 *                 .pathParameter("id")
 *                 .produces("application/json"))
 *         .build();
 *
 * RestResponse<String> response = api.connect(RestAuthentication.none())
 *         .request("getPet")
 *         .pathParameter("id", 42)
 *         .execute(HttpResponse.BodyHandlers.ofString())
 *         .requireSuccess();
 * }</pre>
 *
 * <h2>Importing an OpenAPI 3 document</h2>
 *
 * The builder reads standard OpenAPI 3 JSON or YAML documents. The first absolute URL in
 * {@code servers} supplies the base URI unless one is set explicitly. Endpoint names are the
 * operation IDs; generated names are used when an operation has no ID.
 *
 * <pre>{@code
 * RestApi api;
 * try (InputStream document = Files.newInputStream(Path.of("openapi.yaml"))) {
 *     api = RestApi.builder().fromOpenApi(document).build();
 * }
 *
 * RestResponse<String> response = api.connect()
 *         .request("getPet")
 *         .pathParameter("id", 42)
 *         .execute(HttpResponse.BodyHandlers.ofString());
 * }</pre>
 *
 * <h2>JSON request and response mapping</h2>
 *
 * With Jackson 3 available, {@link RestRequest#json(Object)} and
 * {@link RestRequest#executeJson(Class)} provide optional object mapping:
 *
 * <pre>{@code
 * RestResponse<Pet> response = api.connect()
 *         .request("createPet")
 *         .body(RestBody.text("{\"name\":\"Ada\"}", "application/json"))
 *         .executeJson(Pet.class);
 * }</pre>
 */
@NullMarked
package com.dua3.utility.rest;

import org.jspecify.annotations.NullMarked;

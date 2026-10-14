package com.dua3.utility.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
class RestAuthenticationTest {

    @Test
    void fixedAuthenticationsProduceExpectedCredentials() {
        assertEquals(Map.of(), credentials(RestAuthentication.none()).headers());
        assertEquals("Basic YWxhZGRpbjpvcGVuIHNlc2FtZQ==",
                credentials(RestAuthentication.basic("aladdin", "open sesame")).headers().get("Authorization").getFirst());
        assertEquals("Bearer token", credentials(RestAuthentication.bearer("token"))
                .headers().get("Authorization").getFirst());
        assertEquals("secret", credentials(RestAuthentication.apiKey("X-Api-Key", "secret", RestParameterLocation.HEADER))
                .headers().get("X-Api-Key").getFirst());
        assertEquals("secret", credentials(RestAuthentication.apiKey("key", "secret", RestParameterLocation.QUERY))
                .queryParameters().get("key").getFirst());

        assertThrows(IllegalArgumentException.class,
                () -> RestAuthentication.apiKey("key", "secret", RestParameterLocation.COOKIE));
    }

    @Test
    void bearerAuthenticationCachesUsableTokensAndHonorsForcedRefresh() {
        AtomicInteger providerCalls = new AtomicInteger();
        RestAuthentication authentication = RestAuthentication.bearer(() -> CompletableFuture.completedFuture(new AccessToken(
                "token-" + providerCalls.incrementAndGet(), Instant.now().plusSeconds(60))));

        assertEquals("Bearer token-1", credentials(authentication).headers().get("Authorization").getFirst());
        assertEquals("Bearer token-1", credentials(authentication).headers().get("Authorization").getFirst());
        assertEquals("Bearer token-2", credentials(authentication, true).headers().get("Authorization").getFirst());
        assertEquals(2, providerCalls.get());
    }

    @Test
    void bearerAuthenticationCoalescesConcurrentRefreshesAndAvoidsExpiringTokens() {
        AtomicInteger providerCalls = new AtomicInteger();
        CompletableFuture<AccessToken> pending = new CompletableFuture<>();
        RestAuthentication authentication = RestAuthentication.bearer(() -> {
            providerCalls.incrementAndGet();
            return pending;
        });

        CompletableFuture<RestCredentials> first = authentication.credentials(false).toCompletableFuture();
        CompletableFuture<RestCredentials> second = authentication.credentials(false).toCompletableFuture();
        assertEquals(1, providerCalls.get());
        pending.complete(new AccessToken("shared", Instant.now().plusSeconds(60)));
        assertEquals("Bearer shared", first.join().headers().get("Authorization").getFirst());
        assertEquals("Bearer shared", second.join().headers().get("Authorization").getFirst());

        AtomicInteger expiringProviderCalls = new AtomicInteger();
        RestAuthentication expiring = RestAuthentication.bearer(() -> CompletableFuture.completedFuture(new AccessToken(
                "short-" + expiringProviderCalls.incrementAndGet(), Instant.now().plusSeconds(10))));
        credentials(expiring);
        credentials(expiring);
        assertEquals(2, expiringProviderCalls.get());
    }

    @Test
    void credentialsAreDefensiveAndTokenValuesAreValidated() {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        List<String> values = new ArrayList<>(List.of("first"));
        headers.put("X-Test", values);
        RestCredentials credentials = new RestCredentials(headers, Map.of());
        values.set(0, "changed");
        headers.put("X-Other", List.of("value"));

        assertEquals("first", credentials.headers().get("X-Test").getFirst());
        assertThrows(UnsupportedOperationException.class, () -> credentials.headers().put("X-New", List.of("value")));
        assertThrows(UnsupportedOperationException.class, () -> credentials.headers().get("X-Test").add("second"));
        assertThrows(IllegalArgumentException.class, () -> new AccessToken(" ", null));
        assertTrue(credentials.headers().containsKey("X-Test"));
    }

    private static RestCredentials credentials(RestAuthentication authentication) {
        return credentials(authentication, false);
    }

    private static RestCredentials credentials(RestAuthentication authentication, boolean forceRefresh) {
        return authentication.credentials(forceRefresh).toCompletableFuture().join();
    }
}

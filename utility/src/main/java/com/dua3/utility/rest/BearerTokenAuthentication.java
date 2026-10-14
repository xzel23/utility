package com.dua3.utility.rest;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/** Caches and refreshes bearer tokens for {@link RestAuthentication}. */
final class BearerTokenAuthentication implements RestAuthentication {
    private static final Duration REFRESH_SKEW = Duration.ofSeconds(30);

    private final AccessTokenProvider tokenProvider;
    private final Object lock = new Object();

    private @Nullable AccessToken cachedToken;
    private @Nullable CompletableFuture<AccessToken> refresh;

    BearerTokenAuthentication(AccessTokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
    }

    @Override
    public CompletionStage<RestCredentials> credentials(boolean forceRefresh) {
        return token(forceRefresh).thenApply(token ->
                RestCredentials.header("Authorization", "Bearer " + token.value()));
    }

    @Override
    public boolean refreshable() {
        return true;
    }

    private CompletionStage<AccessToken> token(boolean forceRefresh) {
        synchronized (lock) {
            if (!forceRefresh && isUsable(cachedToken)) {
                assert cachedToken != null;
                //noinspection NullableProblems
                return CompletableFuture.completedFuture(cachedToken);
            }
            if (refresh != null) {
                return refresh;
            }

            CompletableFuture<AccessToken> result;
            try {
                result = Objects.requireNonNull(tokenProvider.getAccessToken(), "tokenProvider returned null")
                        .toCompletableFuture();
            } catch (RuntimeException exception) {
                result = CompletableFuture.failedFuture(exception);
            }

            final CompletableFuture<AccessToken> refreshAttempt = result;
            refresh = refreshAttempt;
            refreshAttempt.whenComplete((token, error) -> {
                synchronized (lock) {
                    if (refresh == refreshAttempt) {
                        if (error == null) {
                            cachedToken = Objects.requireNonNull(token, "tokenProvider completed with null");
                        }
                        refresh = null;
                    }
                }
            });
            return refreshAttempt;
        }
    }

    private static boolean isUsable(@Nullable AccessToken token) {
        if (token == null) {
            return false;
        }
        Instant expiresAt = token.expiresAt();
        return expiresAt == null || expiresAt.isAfter(Instant.now().plus(REFRESH_SKEW));
    }
}

package com.dua3.utility.rest;

import java.util.concurrent.CompletionStage;

/**
 * Obtains an access token. A provider may use a refresh token, client credentials, or another
 * authentication mechanism.
 */
@FunctionalInterface
public interface AccessTokenProvider {

    /**
     * Obtains a current access token.
     *
     * @return a stage that supplies the token
     */
    CompletionStage<AccessToken> getAccessToken();
}

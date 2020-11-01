package org.astropeci.urmwstats.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.astropeci.urmwstats.SecurityConfiguration;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom client for sending OAuth2 token requests to the Discord OAuth2 API.
 *
 * This is necessary since Discord rejects requests without a User-Agent header.
 */
@Component
@RequiredArgsConstructor
public class RestOAuthAccessTokenClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private RestOperations restOperations = new RestTemplate();

    private final Validator validator;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();

        HttpEntity<?> request = new HttpEntity<>(
                getRequestBody(clientRegistration, authorizationGrantRequest),
                getRequestHeaders()
        );

        ResponseEntity<AccessResponse> response = restOperations.exchange(tokenUri, HttpMethod.POST, request, AccessResponse.class);
        AccessResponse accessResponse = response.getBody();

        validateResponse(accessResponse);

        Set<String> scopes;
        if (accessResponse.getScopes().isEmpty()) {
            scopes = authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest().getScopes();
        } else {
            scopes = accessResponse.getScopes();
        }

        return OAuth2AccessTokenResponse.withToken(accessResponse.getAccessToken())
                .tokenType(accessResponse.getTokenType())
                .expiresIn(accessResponse.getExpiresIn())
                .scopes(scopes)
                .build();
    }

    @SneakyThrows
    private void validateResponse(AccessResponse response) {
        if (response == null) {
            throw new IOException("OAuth token request returned no body");
        }

        Set<ConstraintViolation<AccessResponse>> violations = validator.validate(response);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private Object getRequestBody(ClientRegistration clientRegistration, OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();

        tokenRequest.add("client_id", clientRegistration.getClientId());
        tokenRequest.add("client_secret", clientRegistration.getClientSecret());
        tokenRequest.add("grant_type", clientRegistration.getAuthorizationGrantType().getValue());
        tokenRequest.add("code", authorizationGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode());
        tokenRequest.add("redirect_uri", authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());
        tokenRequest.add("scope", String.join(" ", authorizationGrantRequest.getClientRegistration().getScopes()));

        return tokenRequest;
    }

    private HttpHeaders getRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.USER_AGENT, SecurityConfiguration.DISCORD_BOT_USER_AGENT);

        return headers;
    }

    private static class AccessResponse {

        @NotBlank
        @Getter
        @JsonProperty("access_token")
        private String accessToken;

        @Getter
        @JsonProperty("expires_in")
        private int expiresIn;

        @NotBlank
        @JsonProperty("token_type")
        private String tokenType;

        private String scope;

        @SneakyThrows({ IOException.class })
        public OAuth2AccessToken.TokenType getTokenType() {
            String bearerTokenType = OAuth2AccessToken.TokenType.BEARER.getValue();

            if (tokenType.equalsIgnoreCase(bearerTokenType)) {
                return OAuth2AccessToken.TokenType.BEARER;
            } else {
                throw new IOException("Unexpected token_type returned to OAuth token request: " + tokenType);
            }
        }

        public Set<String> getScopes() {
            if (scope == null || scope.isBlank()) {
                return Set.of();
            } else {
                return Stream.of(scope.split("\\s+")).collect(Collectors.toSet());
            }
        }
    }
}
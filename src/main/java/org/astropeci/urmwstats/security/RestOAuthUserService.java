package org.astropeci.urmwstats.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/**
 * Custom client for sending OAuth2 user-info requests to the Discord OAuth2 API.
 *
 * This is necessary since Discord rejects requests without a User-Agent header.
 */
@Slf4j
@Component
public class RestOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private RestOperations restOperations = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String userInfoUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + userRequest.getAccessToken().getTokenValue());
        headers.set(HttpHeaders.USER_AGENT, SecurityConfiguration.DISCORD_BOT_USER_AGENT);

        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> responseEntity = restOperations.exchange(userInfoUri, HttpMethod.GET, new HttpEntity<>(headers), responseType);

        Map<String, Object> userAttributes = responseEntity.getBody();
        if (userAttributes == null) {
            log.warn("No body returned for OAuth user info request, defaulting to no attributes");
            userAttributes = Map.of();
        }

        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));

        String nameAttributeKey = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOAuth2User(authorities, userAttributes, nameAttributeKey);
    }
}

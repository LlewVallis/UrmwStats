package org.astropeci.urmwstats.auth;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.SecretProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

/**
 * Registers the Discord OAuth2 API with Spring.
 */
@Component
@RequiredArgsConstructor
public class CustomClientRegistrationRepository implements ClientRegistrationRepository {

    private final SecretProvider secretProvider;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (registrationId.equals("discord")) {
            return ClientRegistration.withRegistrationId("discord")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .scope("identify")
                    .clientName("URMW Stats")
                    .redirectUriTemplate("{baseUrl}/login/oauth2/code/discord")
                    .authorizationUri("https://discordapp.com/api/oauth2/authorize")
                    .tokenUri("https://discordapp.com/api/oauth2/token")
                    .userInfoUri("https://discordapp.com/api/users/@me")
                    .userNameAttributeName("username")
                    .clientId(secretProvider.getDiscordClientId())
                    .clientSecret(secretProvider.getDiscordClientSecret())
                    .build();
        }

        return null;
    }
}

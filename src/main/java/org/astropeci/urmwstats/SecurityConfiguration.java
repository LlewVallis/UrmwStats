package org.astropeci.urmwstats;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.auth.RestOAuthAccessTokenClient;
import org.astropeci.urmwstats.auth.RestOAuthUserService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Configures the Spring security used in the application.
 */
@Component
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String DISCORD_BOT_USER_AGENT = "Urmw-Stats";

    private final RestOAuthAccessTokenClient tokenClient;
    private final RestOAuthUserService userService;

    private final Environment springEnv;
    private final ClientRegistrationRepository clients;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(a -> a
                        .anyRequest().permitAll()
                )
                .logout(l -> l
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                )
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        if (Set.of(springEnv.getActiveProfiles()).contains("oauth2")) {
            http.oauth2Login(o -> o
                    .tokenEndpoint(t -> t
                            .accessTokenResponseClient(tokenClient)
                    )
                    .userInfoEndpoint(u -> u
                            .userService(userService)
                    )
                    .defaultSuccessUrl("/", true)
                    .clientRegistrationRepository(clients)
            );
        }

        if (Set.of(springEnv.getActiveProfiles()).contains("ssl")) {
            http
                    .requiresChannel()
                    .anyRequest()
                    .requiresSecure();
        }
    }
}

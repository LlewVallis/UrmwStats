package org.astropeci.urmwstats.auth;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Endpoints related to the user's current OAuth2 session.
 */
@RestController
@RequestMapping("/api")
@Secured("ROLE_USER")
@RequiredArgsConstructor
public class OAuthEndpoints {

    private final RoleManager roleManager;

    @Value
    private static class DiscordUserResponse {
        String id;
        String name;
        String discriminator;
        String avatarUri;
        boolean staff;
    }

    @GetMapping("/discord-user")
    public DiscordUserResponse user(@AuthenticationPrincipal OAuth2User principal) {
        String id = Objects.requireNonNull(principal.getAttribute("id"));
        String name = Objects.requireNonNull(principal.getAttribute("username"));
        String discriminator = Objects.requireNonNull(principal.getAttribute("discriminator"));

        String avatarHash = principal.getAttribute("avatar");

        String avatarUri;
        if (avatarHash == null) {
            int modDiscriminator = Integer.parseInt(discriminator) % 5;
            avatarUri = String.format("https://cdn.discordapp.com/embed/avatars/%s.png", modDiscriminator);
        } else {
            avatarUri = String.format("https://cdn.discordapp.com/avatars/%s/%s.png", id, avatarHash);
        }

        boolean staff;
        try {
            roleManager.authenticate(principal);
            staff = true;
        } catch (NotStaffException ignored) {
            staff = false;
        }

        return new DiscordUserResponse(id, name, discriminator, avatarUri, staff);
    }
}

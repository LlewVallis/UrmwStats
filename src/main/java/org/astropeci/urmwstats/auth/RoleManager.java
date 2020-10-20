package org.astropeci.urmwstats.auth;

import com.google.common.collect.MapMaker;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.astropeci.urmwstats.SecretProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleManager {

    private static final Set<String> STAFF_ROLES = Set.of(
            "Administrator",
            "Ranking Manager",
            "Developer",
            "Tourney Manager",
            "Video Manager",
            "Supporting Developer",
            "Information Collector",
            "Collector in Training"
    );

    private final JDA jda;
    private final SecretProvider secretProvider;

    private final Map<String, Result> cache = new MapMaker()
            .expiration(10, TimeUnit.MINUTES)
            .makeComputingMap(this::determineAuthenticationResult);

    @Value
    private static class Result {
        AuthProof proof;
    }

    public AuthProof authenticate(OAuth2User principal) {
        String id = Objects.requireNonNull(principal.getAttribute("id"));
        Result result = cache.get(id);

        if (result.proof == null) {
            throw new NotStaffException();
        } else {
            return result.proof;
        }
    }

    private Result determineAuthenticationResult(String id) {
        log.info("Performing authentication lookup for " + id);

        Guild guild = getGuild();
        if (guild == null) {
            return new Result(null);
        }

        CompletableFuture<Member> memberRequest = guild.retrieveMemberById(id)
                .submit()
                .exceptionally(error -> {
                    log.info("Failed to fetch user for authentication: " + error.getMessage());
                    return null;
                });

        Member member = memberRequest.join();
        if (member == null) {
            return new Result(null);
        }

        Collection<Role> roles = member.getRoles();

        for (Role role : roles) {
            if (STAFF_ROLES.contains(role.getName())) {
                return new Result(new AuthProof());
            }
        }

        return new Result(null);
    }

    private Guild getGuild() {
        List<Guild> guilds = new ArrayList<>(jda.getGuilds());

        String testingGuildId = secretProvider.getTestingGuildId();
        guilds.removeIf(guild -> guild.getId().equals(testingGuildId));

        if (guilds.size() == 0) {
            log.error("The bot is not in any guilds, authentication will fail");
            return null;
        }

        if (guilds.size() > 1) {
            log.warn("The bot is in multiple guilds, authentication will fail");
            return null;
        }

        return guilds.get(0);
    }
}

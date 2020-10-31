package org.astropeci.urmwstats.export;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class UserCache {

    private final JDA jda;
    private final Map<Long, Optional<User>> cache = new HashMap<>();

    public User retrieve(long id) {
        return cache.computeIfAbsent(id, _id ->
                Optional.ofNullable(jda.retrieveUserById(id).submit().exceptionally(error -> null).join())
        ).orElse(null);
    }
}

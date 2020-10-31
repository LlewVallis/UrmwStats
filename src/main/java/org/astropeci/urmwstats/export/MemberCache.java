package org.astropeci.urmwstats.export;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class MemberCache {

    private final Guild guild;
    private final Map<Long, Optional<Member>> cache = new HashMap<>();

    public Member retrieve(long id) {
        return cache.computeIfAbsent(id, _id ->
                Optional.ofNullable(guild.retrieveMemberById(id).submit().exceptionally(error -> null).join())
        ).orElse(null);
    }
}

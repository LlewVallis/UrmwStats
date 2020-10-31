package org.astropeci.urmwstats.api;

import com.google.common.collect.MapMaker;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserEndpoints {

    private final JDA jda;

    private final Map<String, UserData> cache = new MapMaker()
            .expiration(6, TimeUnit.HOURS)
            .makeMap();

    @Value
    private static class UserData {
        String name;
        String avatarUri;
    }

    @PostMapping("/fetch-users")
    public Map<String, UserData> fetchUsers(@RequestBody List<String> ids) {
        return ids.stream().parallel().flatMap(id -> {
            UserData cached = cache.get(id);
            if (cached != null) {
                return Stream.of(Map.entry(id, cached));
            }

            User user;
            try {
                user = jda.retrieveUserById(id).complete();
            } catch (Exception e) {
                return Stream.empty();
            }

            UserData data = new UserData(user.getName(), user.getEffectiveAvatarUrl());

            cache.put(id, data);
            return Stream.of(Map.entry(id, data));
        }).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
    }
}

package org.astropeci.urmwstats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Announcer {

    private final JDA jda;
    private final SecretProvider secretProvider;

    public void announce(String message) {
        String channelId = secretProvider.getAnnouncementChannelId();
        MessageChannel channel = jda.getTextChannelById(channelId);

        if (channel != null) {
            channel.sendMessage("📝 " + message).queue(
                    discordMessage -> { },
                    error -> log.error("Could not post announcement to {}", channelId, error)
            );
        } else {
            log.error("Could not find channel {}", channelId);
        }
    }
}

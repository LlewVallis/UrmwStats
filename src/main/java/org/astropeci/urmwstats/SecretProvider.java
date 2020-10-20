package org.astropeci.urmwstats;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

/**
 * Provides secret values, such as the Discord bot token.
 */
@Component
public class SecretProvider {

    private Dotenv env = Dotenv.load();

    public String getDiscordClientId() {
        return getEnv("DISCORD_CLIENT_ID");
    }

    public String getDiscordClientSecret() {
        return getEnv("DISCORD_CLIENT_SECRET");
    }

    public String getDiscordBotToken() {
        return getEnv("DISCORD_BOT_TOKEN");
    }

    public String getMongoUri() {
        return getEnv("MONGO_URI");
    }

    public String getAnnouncementChannelId() {
        return getEnv("ANNOUNCEMENT_CHANNEL_ID");
    }

    public String getTestingGuildId() {
        return getEnv("TESTING_GUILD_ID");
    }

    private String getEnv(String variableName) {
        String value = env.get(variableName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable " + variableName);
        }

        return value;
    }
}

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

    public String getTestingGuildId() {
        return getEnv("TESTING_GUILD_ID");
    }

    public String getAnnouncementChannelId() {
        return getEnv("ANNOUNCEMENT_CHANNEL_ID");
    }

    public String getTourneyChannelId() {
        return getEnv("TOURNEY_CHANNEL_ID");
    }

    public String getReactionRoleEmote() {
        return getEnv("REACTION_ROLE_EMOTE");
    }

    public String getReactionRoleMessageId() {
        return getEnv("REACTION_ROLE_MESSAGE_ID");
    }

    public int getProductionHttpsPort() {
        return getEnvInt("PRODUCTION_HTTPS_PORT");
    }

    public String getReactionRoleRoleId() {
        return getEnv("REACTION_ROLE_ROLE_ID");
    }

    private String getEnv(String variableName) {
        String value = env.get(variableName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable " + variableName);
        }

        return value;
    }

    private int getEnvInt(String variableName) {
        String stringValue = getEnv(variableName);
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer in variable " + variableName);
        }
    }
}

package org.astropeci.urmwstats;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Slf4j
@Configuration
public class JdaProvider {

    @Bean
    @SneakyThrows({ InterruptedException.class, LoginException.class })
    public JDA createJda(SecretProvider secretProvider) {
        log.info("Connecting to the Discord API");
        JDA jda = JDABuilder.createLight(
                secretProvider.getDiscordBotToken(),
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        ).enableCache(
                CacheFlag.VOICE_STATE
        ).setMemberCachePolicy(MemberCachePolicy.VOICE).build();

        jda.getPresence().setActivity(Activity.playing("%help"));
        jda.awaitReady();

        return jda;
    }
}

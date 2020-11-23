package org.astropeci.urmwstats.reactionroles;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.astropeci.urmwstats.SecretProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;

@Slf4j
@Component
public class ReactionRoleHandler extends ListenerAdapter {

    private final String reactionRoleEmote;
    private final String reactionRoleMessageId;
    private final String reactionRoleRoleId;

    public ReactionRoleHandler(SecretProvider secretProvider, JDA jda) {
        jda.addEventListener(this);
        reactionRoleEmote = secretProvider.getReactionRoleEmote();
        reactionRoleMessageId = secretProvider.getReactionRoleMessageId();
        reactionRoleRoleId = secretProvider.getReactionRoleRoleId();
    }

    private boolean shouldModifyRole(GenericGuildMessageReactionEvent event) {
        return event.getReactionEmote().getName().equals(reactionRoleEmote)
                && event.getMessageId().equals(reactionRoleMessageId);
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!shouldModifyRole(event)) {
            return;
        }

        log.info("Adding reaction role for {}", event.getUserId());

        Guild guild = event.getGuild();
        Role role = Objects.requireNonNull(guild.getRoleById(reactionRoleRoleId), "reaction role does not exist");
        guild.addRoleToMember(event.getUserId(), role).queue();
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        if (!shouldModifyRole(event)) {
            return;
        }

        log.info("Removing reaction role for {}", event.getUserId());

        Guild guild = event.getGuild();
        Role role = Objects.requireNonNull(guild.getRoleById(reactionRoleRoleId), "reaction role does not exist");
        guild.removeRoleFromMember(event.getUserId(), role).queue();
    }
}

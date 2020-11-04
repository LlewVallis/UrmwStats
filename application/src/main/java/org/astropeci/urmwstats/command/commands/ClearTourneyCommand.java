package org.astropeci.urmwstats.command.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.SecretProvider;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ClearTourneyCommand implements Command {

    private final MessageChannel tourneyChannel;

    public ClearTourneyCommand(SecretProvider secretProvider, JDA jda) {
        String tourneyChannelId = secretProvider.getTourneyChannelId();
        tourneyChannel = jda.getTextChannelById(tourneyChannelId);
    }

    @Override
    public String label() {
        return "clear-tourney";
    }

    @Override
    public String usage() {
        return "clear-tourney";
    }

    @Override
    public String helpDescription() {
        return String.format("Clear all messages in <#%s> except the top one", tourneyChannel.getId());
    }

    @Override
    public HelpSection section() {
        return HelpSection.UTILITY;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        MessageHistory beginningHistory = tourneyChannel.getHistoryFromBeginning(2).complete();
        if (beginningHistory.size() < 2) {
            event.getChannel().sendMessage("🌤️ The channel is already clear").queue();
            return;
        }

        Message firstMessage = beginningHistory.getRetrievedHistory().get(1);

        List<Message> messagesToPurge = tourneyChannel.getHistory().retrievePast(100).complete();
        messagesToPurge.remove(firstMessage);

        event.getChannel().sendMessageFormat("💥 Clearing `%s` entries", messagesToPurge.size()).queue();
        tourneyChannel.purgeMessages(messagesToPurge).forEach(CompletableFuture::join);
        event.getChannel().sendMessage("🌤️ The channel is now clear").queue();
    }
}

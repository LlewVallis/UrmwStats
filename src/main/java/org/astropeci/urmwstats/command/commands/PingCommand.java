package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PingCommand implements Command {

    private final JDA jda;

    @Override
    public String label() {
        return "ping";
    }

    @Override
    public String usage() {
        return "ping";
    }

    @Override
    public String helpDescription() {
        return "Shows the bot's ping to Discord";
    }

    @Override
    public int helpPriority() {
        return 1;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        event.getChannel().sendMessage(String.format(
                "⏲️ The bot has a gateway ping of `%s` milliseconds",
                jda.getGatewayPing()
        )).queue();
    }
}

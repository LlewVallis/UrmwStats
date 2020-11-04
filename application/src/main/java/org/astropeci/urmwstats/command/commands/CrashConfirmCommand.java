package org.astropeci.urmwstats.command.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CrashConfirmCommand implements Command {

    @Override
    public String label() {
        return "crash-immediately-i-know-what-i-am-doing";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String helpDescription() {
        return "";
    }

    @Override
    public HelpSection section() {
        return null;
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

        log.error("{} ({}) is crashing the service!", event.getAuthor().getName(), event.getAuthor().getName());

        event.getChannel().sendMessage("🔥 Crashing the service <@305992421118967821>").complete();
        System.exit(1);
    }
}

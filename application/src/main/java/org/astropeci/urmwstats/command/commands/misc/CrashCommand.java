package org.astropeci.urmwstats.command.commands.misc;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrashCommand implements Command {

    @Override
    public String label() {
        return "crash-immediately";
    }

    @Override
    public String usage() {
        return "crash-immediately";
    }

    @Override
    public String helpDescription() {
        return "Instantly crash the entire URMW Stats service";
    }

    @Override
    public HelpSection section() {
        return HelpSection.MISC;
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

        event.getChannel().sendMessage(
                "⚠️ **Are you sure?**\n" +
                        "This will instantly crash the entire URMW Stats service, including the website and bot.\n" +
                        "The service will **not** automatically restart after doing this.\n" +
                        "To continue, run `%crash-immediately-i-know-what-i-am-doing`"
        ).queue();
    }
}

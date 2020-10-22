package org.astropeci.urmwstats.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.auth.RoleManager;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class HelpCommand implements Command {

    private final List<Command> commands;
    private final RoleManager roleManager;

    @Override
    public String label() {
        return "help";
    }

    @Override
    public String usage() {
        return "help [search]";
    }

    @Override
    public String helpDescription() {
        return "Lists all the commands you can use";
    }

    @Override
    public int helpPriority() {
        return 0;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 0 && arguments.size() != 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        final String search;
        if (arguments.size() >= 1) {
            search = arguments.get(0);
        } else {
            search = "";
        }

        boolean userIsStaff = roleManager.isAuthenticated(event.getAuthor().getId());

        List<Command> relevantCommands = commands.stream()
                .filter(command -> !command.isStaffOnly() || userIsStaff)
                .filter(command -> containsIgnoreCase(command.label(), search))
                .sorted(Comparator.comparingInt(Command::helpPriority).reversed())
                .collect(Collectors.toList());

        if (relevantCommands.size() == 0) {
            throw new CommandException("🤷 No command matched that search");
        }

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder().setTitle("Command help");

        for (Command command : relevantCommands) {
            embed.addField("`" + command.usage() + "`", command.helpDescription(), true);
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        Pattern regex = Pattern.compile(Pattern.quote(needle), Pattern.CASE_INSENSITIVE);
        return regex.matcher(haystack).find();
    }
}

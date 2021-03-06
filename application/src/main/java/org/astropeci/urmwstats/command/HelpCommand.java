package org.astropeci.urmwstats.command;

import lombok.RequiredArgsConstructor;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import me.xdrop.fuzzywuzzy.ratios.SimpleRatio;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.auth.RoleManager;

import java.util.Comparator;
import java.util.List;
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
    public HelpSection section() {
        return HelpSection.MISC;
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

        boolean userIsStaff = roleManager.isAuthenticated(event.getAuthor().getId());

        List<Command> relevantCommands = commands.stream()
                .filter(command -> command.section() != null)
                .filter(command -> !command.isStaffOnly() || userIsStaff)
                .sorted(Comparator.comparingInt(
                        (Command command) -> command.helpDescription().length()
                ).reversed())
                .sorted(Comparator.comparingInt(
                        (Command command) -> command.section().ordinal()
                ))
                .collect(Collectors.toList());

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder();

        if (arguments.size() == 1) {
            String search = arguments.get(0);

            BoundExtractedResult<Command> searchResult = FuzzySearch.extractOne(
                    search.toLowerCase(),
                    relevantCommands,
                    Command::label,
                    new SimpleRatio()
            );

            if (searchResult.getScore() < 75) {
                throw new CommandException(String.format(
                        "🔍 Could not find `%s`, try refining your search",
                        search
                ));
            }

            Command command = searchResult.getReferent();

            embed.setTitle("📖 Help for " + command.usage());
            embed.setDescription(command.helpDescription());
        } else {
            embed.setTitle("📖 Command help");

            HelpSection previousSection = HelpSection.GLOBAL;
            for (Command command : relevantCommands) {
                HelpSection newSection = command.section();
                if (newSection != previousSection) {
                    embed.addField("", "__**" + newSection.getTitle() + "**__", false);
                    previousSection = newSection;
                }

                String description = truncateDescription(command.helpDescription());
                embed.addField(command.usage(), description, true);
            }
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private String truncateDescription(String description) {
        if (description.length() > 60) {
            return description.substring(0, 57) + "...";
        } else {
            return description;
        }
    }
}

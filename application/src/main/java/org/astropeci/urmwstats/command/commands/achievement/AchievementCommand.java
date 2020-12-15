package org.astropeci.urmwstats.command.commands.achievement;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.data.Achievement;
import org.astropeci.urmwstats.data.AchievementRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AchievementCommand implements Command {

    private static final int PAGE_SIZE = 30;
    private static final int TWO_COLUMN_THRESHOLD = 5;
    private static final int THREE_COLUMN_THRESHOLD = 10;

    private final AchievementRepository achievementRepository;

    @Override
    public String label() {
        return "achievement";
    }

    @Override
    public String usage() {
        return "achievement <achievement> [page]";
    }

    @Override
    public String helpDescription() {
        return "Shows an achievement's description and completers";
    }

    @Override
    public HelpSection section() {
        return HelpSection.ACHIEVEMENT;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() < 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        int page = 0;
        boolean pageSpecified = false;
        if (arguments.size() > 1) {
            String lastArgument = arguments.get(arguments.size() - 1);
            try {
                page = Integer.parseInt(lastArgument) - 1;
                pageSpecified = true;
            } catch (NumberFormatException ignored) { }
        }

        if (page < 0) {
            throw new CommandException("❌ Pages start at 1");
        }

        if (pageSpecified) {
            arguments.remove(arguments.size() - 1);
        }

        String fuzzyAchievementName = String.join(" ", arguments);

        Achievement achievement = CommandUtil.matchAchievement(achievementRepository, fuzzyAchievementName);
        if (achievement == null) {
            throw new CommandException(String.format(
                    "🔍 Could not find `%s`, try refining your search",
                    fuzzyAchievementName
            ));
        }

        List<String> allCompleters = achievement.getPlayersCompleted();
        int pages = (int) Math.ceil((double) allCompleters.size() / PAGE_SIZE);

        if (page >= Math.max(pages, 1)) {
            if (pages > 1) {
                throw new CommandException(String.format("❌ There are only %s pages available", pages));
            } else {
                throw new CommandException("❌ There is only 1 page");
            }
        }

        List<String> listedCompleters = allCompleters.stream()
                .skip(page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .collect(Collectors.toList());

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle(achievement.getName(), "https://urmw.live/achievements");

        if (achievement.getDescription() == null) {
            embed.setDescription("*This achievement prefers to maintain an air of mystery...*");
        } else {
            embed.setDescription(achievement.getDescription());
        }

        if (listedCompleters.isEmpty()) {
            embed.addField("Completers", "No one has completed this achievement", false);
        } else {
            int leftToCenter;
            int centerToRight;

            if (listedCompleters.size() > THREE_COLUMN_THRESHOLD) {
                leftToCenter = (int) Math.ceil(listedCompleters.size() * 0.33);
                centerToRight = (int) Math.ceil(listedCompleters.size() * 0.66);
            } else if (listedCompleters.size() > TWO_COLUMN_THRESHOLD) {
                leftToCenter = (int) Math.ceil(listedCompleters.size() * 0.5);
                centerToRight = listedCompleters.size();
            } else {
                leftToCenter = listedCompleters.size();
                centerToRight = listedCompleters.size();
            }

            List<String> leftColumn = listedCompleters.subList(0, leftToCenter);
            List<String> centerColumn = listedCompleters.subList(leftToCenter, centerToRight);
            List<String> rightColumn = listedCompleters.subList(centerToRight, listedCompleters.size());

            String title;
            if (pages > 1) {
                title = String.format(
                        "Completers (%s-%s of %s)",
                        page * PAGE_SIZE + 1,
                        Math.min(page * PAGE_SIZE + PAGE_SIZE, allCompleters.size()),
                        allCompleters.size()
                );
            } else {
                title = String.format("Completers (%s)", allCompleters.size());
            }

            embed.addField(title, formatAsList(leftColumn), true);
            embed.addField("", formatAsList(centerColumn), true);
            embed.addField("", formatAsList(rightColumn), true);
        }

        if (pages > 1) {
            embed.setFooter(String.format(
                    "Page %s of %s",
                    page + 1,
                    pages
            ));
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private String formatAsList(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() != 0) {
                result.append("\n");
            }

            result.append("• ").append(value);
        }

        return result.toString();
    }
}

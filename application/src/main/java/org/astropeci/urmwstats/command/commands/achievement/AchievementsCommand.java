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
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.PlayerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AchievementsCommand implements Command {

    private static final int PAGE_SIZE = 20;
    private static final int TWO_COLUMN_THRESHOLD = 5;
    private static final int MAX_ACHIEVEMENT_NAME_LENGTH = 45;

    private final PlayerRepository playerRepository;
    private final AchievementRepository achievementRepository;

    @Override
    public String label() {
        return "achievements";
    }

    @Override
    public String usage() {
        return "achievements [player] [page]";
    }

    @Override
    public String helpDescription() {
        return "Shows all achievements, or a player's achievements";
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
        if (arguments.size() > 2) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        int page = 0;
        boolean pageSpecified = false;
        if (!arguments.isEmpty()) {
            String lastArgument = arguments.get(arguments.size() - 1);
            try {
                page = Integer.parseInt(lastArgument) - 1;
                pageSpecified = true;
            } catch (NumberFormatException e) {
                if (arguments.size() > 1) {
                    throw new CommandException(String.format("❌ `%s` is not a valid page", lastArgument));
                }
            }
        }

        if (page < 0) {
            throw new CommandException("❌ Pages start at 1");
        }

        String playerName;
        if (arguments.size() > 1 || (arguments.size() > 0 && !pageSpecified)) {
            String fuzzyPlayerName = arguments.get(0);

            Player player = CommandUtil.matchPlayer(playerRepository, fuzzyPlayerName);
            if (player == null) {
                throw new CommandException(String.format(
                        "🔍 Could not find `%s`, try refining your search",
                        fuzzyPlayerName
                ));
            }

            playerName = player.getName();
        } else {
            playerName = null;
        }

        List<String> allAchievements = achievementRepository.byName().stream()
                .filter(achievement -> playerName == null || achievement.getPlayersCompleted().contains(playerName))
                .map(Achievement::getName)
                .map(this::truncateName)
                .collect(Collectors.toList());

        List<String> listedAchievements = allAchievements.stream()
                .skip(page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .collect(Collectors.toList());

        int pages = (int) Math.ceil((double) allAchievements.size() / PAGE_SIZE);

        if (page >= Math.max(pages, 1)) {
            if (pages > 1) {
                throw new CommandException(String.format("❌ There are only %s pages available", pages));
            } else {
                throw new CommandException("❌ There is only 1 page");
            }
        }

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder();

        if (playerName == null) {
            embed.setTitle("Achievements", "https://urmw.live/achievements");
        } else {
            embed.setTitle("Achievements for " + playerName, "https://urmw.live/player/" + playerName);
        }

        if (listedAchievements.isEmpty()) {
            embed.setDescription(playerName + " hasn't completed any achievements");
        } else {
            int center;
            if (listedAchievements.size() > TWO_COLUMN_THRESHOLD) {
                center = (int) Math.ceil(listedAchievements.size() * 0.5);
            } else {
                center = listedAchievements.size();
            }

            List<String> leftColumn = listedAchievements.subList(0, center);
            List<String> rightColumn = listedAchievements.subList(center, listedAchievements.size());

            String title;
            if (pages > 1) {
                title = String.format(
                        "Showing %s-%s of %s achievements",
                        page * PAGE_SIZE + 1,
                        Math.min(page * PAGE_SIZE + PAGE_SIZE, allAchievements.size()),
                        allAchievements.size()
                );
            } else {
                title = String.format("Showing %s achievements", allAchievements.size());
            }

            embed.addField(title, CommandUtil.formatAsList(leftColumn), true);
            embed.addField("", CommandUtil.formatAsList(rightColumn), true);
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

    private String truncateName(String name) {
        if (name.length() > MAX_ACHIEVEMENT_NAME_LENGTH) {
            return name.substring(0, MAX_ACHIEVEMENT_NAME_LENGTH - 3) + "...";
        } else {
            return name;
        }
    }
}

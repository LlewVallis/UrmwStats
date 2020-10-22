package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.data.*;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatsCommand implements Command {

    private final RepositoryCoordinator repositoryCoordinator;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TourneyRepository tourneyRepository;

    @Override
    public String label() {
        return "stats";
    }

    @Override
    public String usage() {
        return "stats";
    }

    @Override
    public String helpDescription() {
        return "Shows statistics for the season";
    }

    @Override
    public int helpPriority() {
        return 4;
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

        executeGlobalStats(event);
    }

    private void executeGlobalStats(MessageReceivedEvent event) {
        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle("📊 Season statistics", "https://urmw.live");

        appendLastTourneyPlacings(embed);
        appendTopPlayers(embed);
        appendTopTourneyWinners(embed);
        appendLongestStreaks(embed);
        appendMostWins(embed);
        appendMostActive(embed);

        embed.setFooter(String.format(
                "Tracking %s players, %s matches and %s tourneys",
                playerRepository.size(),
                matchRepository.size(),
                tourneyRepository.size()
        ));

        embed.setTimestamp(repositoryCoordinator.getLastUpdated());

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void appendTopPlayers(EmbedBuilder embed) {
        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .limit(3)
                .map(player -> String.format(
                        "%s (%.0f, %.0f)",
                        player.getName(),
                        player.getSkill().getTrueskill(),
                        player.getSkill().getDeviation()
                ))
                .collect(Collectors.toList());

        if (playerStrings.size() >= 3) {
            embed.addField("Highest rated players", rankingString(playerStrings), true);
        }
    }

    private void appendLastTourneyPlacings(EmbedBuilder embed) {
        tourneyRepository.mostRecent(1, null).stream().findFirst().ifPresent(tourney -> {
            List<Set<String>> teams = List.of(tourney.getFirst(), tourney.getSecond(), tourney.getThird());
            List<String> teamStrings = teams.stream()
                    .limit(3)
                    .map(team -> String.join(", ", team))
                    .collect(Collectors.toList());

            embed.addField("Last tourney placings", rankingString(teamStrings), true);
        });
    }

    private void appendTopTourneyWinners(EmbedBuilder embed) {
        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .sorted(Comparator.comparingDouble(Player::getFractionalTourneyWins).reversed())
                .limit(3)
                .filter(player -> player.getFractionalTourneyWins() > 0)
                .map(player -> String.format(
                        "%s (%s)",
                        player.getName(),
                        new DecimalFormat("#.##").format(player.getFractionalTourneyWins())
                ))
                .collect(Collectors.toList());

        if (playerStrings.size() > 0) {
            embed.addField("Top tourney winners", rankingString(playerStrings), true);
        }
    }

    private void appendLongestStreaks(EmbedBuilder embed) {
        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .sorted(Comparator.comparingInt(Player::getStreak).reversed())
                .limit(3)
                .filter(player -> player.getStreak() > 1)
                .map(player -> String.format(
                        "%s (%s)",
                        player.getName(),
                        player.getStreak()
                ))
                .collect(Collectors.toList());

        if (playerStrings.size() > 0) {
            embed.addField("Longest win streaks", rankingString(playerStrings), true);
        }
    }

    private void appendMostWins(EmbedBuilder embed) {
        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .sorted(Comparator.comparingInt(Player::getWins).reversed())
                .limit(3)
                .filter(player -> player.getWins() > 0)
                .map(player -> String.format(
                        "%s (%s)",
                        player.getName(),
                        player.getWins()
                ))
                .collect(Collectors.toList());

        if (playerStrings.size() > 0) {
            embed.addField("Most wins", rankingString(playerStrings), true);
        }
    }

    private void appendMostActive(EmbedBuilder embed) {
        Instant oneMonthAgo = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant();

        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .sorted(Comparator.<Player>comparingInt(player ->
                        recentMatches(player, oneMonthAgo)
                ).reversed())
                .limit(3)
                .filter(player -> recentMatches(player, oneMonthAgo) > 0)
                .map(Player::getName)
                .collect(Collectors.toList());

        if (playerStrings.size() > 0) {
            embed.addField("Most active monthly", rankingString(playerStrings), true);
        }
    }

    private int recentMatches(Player player, Instant cutoff) {
        return (int) matchRepository.mostRecent(Integer.MAX_VALUE, player.getName()).stream()
                .filter(match -> match.getTimestamp().isAfter(cutoff))
                .count();
    }

    private String rankingString(List<String> values) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            result.append(String.format(
                    "%s**%s%s**: %s",
                    i == 0 ? "" : "\n",
                    i + 1,
                    i == 0 ? "st" : i == 1 ? "nd" : i == 2 ? "rd" : "th",
                    values.get(i)
            ));
        }

        return result.toString();
    }
}

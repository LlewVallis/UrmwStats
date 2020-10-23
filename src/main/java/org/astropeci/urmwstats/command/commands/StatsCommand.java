package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.data.*;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        return "stats [player]";
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
        if (arguments.size() != 0 && arguments.size() != 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        if (arguments.size() == 0) {
            executeGlobalStats(event);
        } else {
            executePlayerStats(arguments.get(0), event);
        }
    }

    private void executePlayerStats(String fuzzyPlayerName, MessageReceivedEvent event) {
        Player player = CommandUtil.matchPlayer(playerRepository, fuzzyPlayerName).orElseThrow(() ->
                new CommandException(String.format(
                        "🔍 Could not find `%s`, try refining your search",
                        fuzzyPlayerName
                ))
        );

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle("📈 Statistics for " + player.getName(), "https://urmw.live/player/" + player.getName());

        embed.addField("Skill", String.format(
                "%.0f, %.0f",
                player.getSkill().getTrueskill(),
                player.getSkill().getDeviation()
        ), true);

        embed.addField("Peak skill", String.format(
                "%.0f, %.0f",
                player.getPeakSkill().getTrueskill(),
                player.getPeakSkill().getDeviation()
        ), true);

        String rankName = player.getRankName().substring(0, 1).toUpperCase() + player.getRankName().substring(1);
        int ranking = player.getRanking() + 1;
        embed.addField("Ranking", String.format(
                "%s, %s%s",
                rankName,
                ranking,
                CommandUtil.ordinalSuffix(ranking)
        ), true);

        int streak = player.getStreak();
        String streakType = streak == 1 ? "win" : streak == -1 ? "loss" : streak < 0 ? "losses" : "wins";
        embed.addField("Matches", String.format(
                "**Won**: %s matches\n**Lost**: %s matches\n**Streak**: %s %s",
                player.getWins(),
                player.getLosses(),
                Math.abs(player.getStreak()),
                streakType
        ), true);

        embed.addField("Tourney placings", String.format(
                "**1st**: %s times\n**2nd**: %s times\n**3rd**: %s times",
                player.getTimesPlacedFirst(),
                player.getTimesPlacedSecond(),
                player.getTimesPlacedThird()
        ), true);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void executeGlobalStats(MessageReceivedEvent event) {
        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle("📊 Season statistics", "https://urmw.live");

        appendLastTourneyPlacings(embed);
        appendTopPlayers(embed);
        appendTopTourneyWinners(embed);
        appendLongestStreaks(embed);
        appendMostWins(embed);
        appendHighestWinRate(embed);

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

    private void appendHighestWinRate(EmbedBuilder embed) {
        List<String> playerStrings = playerRepository.getPlayersByRanking().stream()
                .filter(player -> player.getWins() + player.getLosses() >= 10)
                .sorted(Comparator.comparingDouble(this::winRate).reversed())
                .limit(3)
                .map(player -> String.format(
                        "%s (%.2f%%)",
                        player.getName(),
                        winRate(player) * 100
                ))
                .collect(Collectors.toList());

        if (playerStrings.size() > 0) {
            embed.addField("Highest win rate", rankingString(playerStrings), true);
        }
    }

    private double winRate(Player player) {
        return (double) player.getWins() / (player.getWins() + player.getLosses());
    }

    private String rankingString(List<String> values) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            result.append(String.format(
                    "%s**%s%s**: %s",
                    i == 0 ? "" : "\n",
                    i + 1,
                    CommandUtil.ordinalSuffix(i + 1),
                    values.get(i)
            ));
        }

        return result.toString();
    }
}

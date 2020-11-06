package org.astropeci.urmwstats.command.commands.global;

import de.gesundkrank.jskills.*;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.TrueskillSettings;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.PlayerRepository;
import org.astropeci.urmwstats.data.RepositoryCoordinator;
import org.astropeci.urmwstats.data.Skill;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CalculateCommand implements Command {

    private final PlayerRepository playerRepository;

    @Value
    private static class PlayerId implements IPlayer {
        String name;
        boolean winner;
    }

    @Override
    public String label() {
        return "calc";
    }

    @Override
    public String usage() {
        return "calc <team1> <team2>";
    }

    @Override
    public String helpDescription() {
        return "Calculate the result of a match with two arbitrary teams";
    }

    @Override
    public HelpSection section() {
        return HelpSection.GLOBAL;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 2)  {
            CommandUtil.throwWrongNumberOfArguments();
        }

        Map<String, Player> team1Players = parsePlayers(arguments.get(0));
        Map<String, Player> team2Players = parsePlayers(arguments.get(1));

        String team1String = String.join(", ", team1Players.keySet());
        String team2String = String.join(", ", team2Players.keySet());

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder().setTitle(String.format(
                "⚔ %s vs %s",
                team1String,
                team2String
        ));

        if (team1Players.keySet().size() != team2Players.keySet().size()) {
            embed.setDescription("⚠️ Unbalanced match, this probably won't make any sense");
        }

        embed.addField(String.format(
                "If %s wins",
                team1String
        ), resultString(team1Players, team2Players), false);

        embed.addField(String.format(
                "If %s wins",
                team2String
        ), resultString(team2Players, team1Players), false);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private String resultString(Map<String, Player> winners, Map<String, Player> losers) {
        TrueskillSettings trueskillSettings = new TrueskillSettings();
        GameInfo gameInfo = new GameInfo(
                trueskillSettings.getMu(),
                trueskillSettings.getSigma(),
                trueskillSettings.getBeta(),
                trueskillSettings.getTau(),
                trueskillSettings.getDrawProbability()
        );

        Team winnersTeam = createTeam(winners, true);
        Team losersTeam = createTeam(losers, false);

        Map<IPlayer, Skill> newRatings = TrueSkillCalculator.calculateNewRatings(
                gameInfo,
                List.of(winnersTeam, losersTeam),
                0, 1
        ).entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    double trueskill = Math.ceil(entry.getValue().getMean() - 3 * entry.getValue().getStandardDeviation());
                    double deviation = Math.ceil(entry.getValue().getStandardDeviation());
                    return Skill.fromTrueskill(trueskill, deviation);
                }
        ));

        StringBuilder result = new StringBuilder();

        Function<Boolean, BiConsumer<String, Player>> processor = winningTeam -> (name, player) -> {
            Skill newSkill = newRatings.get(new PlayerId(name, winningTeam));

            double deltaTrueskill = newSkill.getTrueskill() - player.getSkill().getTrueskill();
            double deltaDeviation = newSkill.getDeviation() - player.getSkill().getDeviation();

            result.append(String.format(
                    "%s**%s** %s%.0f, %s%.0f *(%.0f, %.0f → %.0f, %.0f)*",
                    result.length() > 0 ? "\n" : "",
                    name,
                    deltaTrueskill >= 0 ? "+" : "",
                    deltaTrueskill,
                    deltaDeviation >= 0 ? "+" : "",
                    deltaDeviation,
                    player.getSkill().getTrueskill(),
                    player.getSkill().getDeviation(),
                    newSkill.getTrueskill(),
                    newSkill.getDeviation()
            ));
        };

        winners.forEach(processor.apply(true));
        losers.forEach(processor.apply(false));

        return result.toString();
    }

    private Map<String, Player> parsePlayers(String input) {
        Map<String, Player> players = new LinkedHashMap<>();

        for (String fuzzyName : input.split(",")) {
            if (fuzzyName.isBlank()) {
                continue;
            }

            Player player = CommandUtil.matchPlayer(playerRepository, fuzzyName);
            if (player == null) {
                throw new CommandException(String.format(
                        "🔍 Could not find `%s`, try refining your search",
                        fuzzyName
                ));
            }

            players.put(player.getName(), player);
        }

        return players;
    }

    private Team createTeam(Map<String, Player> players, boolean winners) {
        Team team = new Team();

        for (Player player : players.values()) {
            team.addPlayer(
                    new PlayerId(player.getName(), winners),
                    new Rating(player.getSkill().getMean(), player.getSkill().getDeviation())
            );
        }

        return team;
    }
}

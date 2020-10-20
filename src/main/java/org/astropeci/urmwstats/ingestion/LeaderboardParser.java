package org.astropeci.urmwstats.ingestion;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.Skill;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LeaderboardParser {

    private static final Pattern REGEX = Pattern.compile("(?<name>[^\\s:]+): (?<trueskill>\\d+) RD (?<deviation>\\d+)");

    public List<Player> parse(List<Message> messages) {
        List<Player> players = parsePlayers(messages);
        updatePlayers(players);
        return players;
    }

    private List<Player> parsePlayers(List<Message> messages) {
        List<Player> players = new ArrayList<>();

        List<String> lines = messages.stream()
                .flatMap(msg -> msg.getContentRaw().lines())
                .collect(Collectors.toList());

        for (String line : lines) {
            Matcher matcher = REGEX.matcher(line);

            if (!matcher.matches()) {
                continue;
            }

            String name = matcher.group("name");
            if (name.equalsIgnoreCase("diffy")) {
                continue;
            }

            String trueskillString = matcher.group("trueskill");
            String deviationString = matcher.group("deviation");

            double trueskill, deviation;
            try {
                trueskill = Double.parseDouble(trueskillString);
                deviation = Double.parseDouble(deviationString);
            } catch (NumberFormatException e) {
                log.warn("Malformed leaderboard line '" + line + "'", e);
                continue;
            }

            Skill skill = Skill.fromTrueskill(trueskill, deviation);
            Player player = new Player(
                    name,
                    skill, skill,
                    0,
                    0, 0,
                    0.0, 0, 0, 0,
                    new HashMap<>(), new HashMap<>(),
                    0
            );

            players.add(player);
        }

        return players;
    }

    private void updatePlayers(List<Player> players) {
        players.sort(Comparator.comparingDouble(player -> -player.getSkill().getTrueskill()));

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            Player previous = i == 0 ? null : players.get(i - 1);
            if (previous != null && previous.getSkill().getTrueskill() == player.getSkill().getTrueskill()) {
                player.setRanking(previous.getRanking());
            } else {
                player.setRanking(i);
            }
        }
    }
}

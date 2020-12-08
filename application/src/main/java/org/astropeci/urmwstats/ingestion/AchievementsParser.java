package org.astropeci.urmwstats.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.data.Achievement;
import org.astropeci.urmwstats.data.Player;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementsParser {

    private static final int MATCH_WARN_THRESHOLD = 85;

    private final PlayerRenamer playerRenamer;

    public List<Achievement> parse(List<Message> messages, List<Player> players) {
        Map<String, Player> playersByName = players.stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));

        playerRenamer.addRenames(playersByName, true);

        if (!messages.isEmpty()) {
            messages.remove(0);
        }

        List<Achievement> achievements = new ArrayList<>();
        for (Message message : messages) {
            List<String> lines = message.getContentStripped().lines().collect(Collectors.toList());

            if (lines.size() < 2) {
                log.warn("Achievement message with less than two lines:\n{}", message.getContentStripped());
                continue;
            }

            String name = cleanString(lines.get(0));
            List<Player> playersCompleted = new ArrayList<>();

            boolean doneLineUsed = false;
            String lastLine = lines.get(lines.size() - 1);
            if (startsWithIgnoreCase(lastLine, "done")) {
                playersCompleted = parseDoneLine(lastLine, playersByName);
                doneLineUsed = true;
            }

            for (Player player : playersCompleted) {
                player.getCompletedAchievements().add(name);
            }

            int descriptionEndLine = doneLineUsed ? lines.size() - 1 : lines.size();
            String description = lines.subList(1, descriptionEndLine).stream()
                    .map(this::cleanString)
                    .collect(Collectors.joining(" "))
                    .trim();

            if (description.equals("???")) {
                description = null;
            }

            List<String> playersCompletedNames = playersCompleted.stream()
                    .map(Player::getName)
                    .sorted(Comparator.comparing(String::toString))
                    .collect(Collectors.toList());

            achievements.add(new Achievement(name, description, playersCompletedNames));
        }

        for (Player player : playersByName.values()) {
            if (player != null) {
                player.getCompletedAchievements().sort(Comparator.comparing(String::toString));
            }
        }

        achievements.sort(Comparator.comparing(Achievement::getName));
        return achievements;
    }

    private List<Player> parseDoneLine(String line, Map<String, Player> playersByName) {
        String[] headerAndBody = line.split(":");

        if (headerAndBody.length != 2) {
            log.warn("Malformed achievement done line: {}", line);
            return List.of();
        }

        String playerList = headerAndBody[1];
        String[] playerNamesRaw = playerList.split(",");

        List<Player> result = new ArrayList<>();

        for (String playerName : playerNamesRaw) {
            playerName = cleanString(playerName);
            Player player = lookupPlayer(playerName, playersByName);

            if (player != null) {
                result.add(player);
            }
        }

        return result;
    }

    private Player lookupPlayer(String fuzzyName, Map<String, Player> playersByName) {
        if (playersByName.size() == 0) {
            log.warn("Achievement messages cannot be parsed when no players exist");
            return null;
        }

        BoundExtractedResult<String> searchResult = FuzzySearch.extractOne(
                fuzzyName.toLowerCase(),
                playersByName.keySet(),
                (ToStringFunction<String>) String::toLowerCase
        );

        String name = searchResult.getReferent();

        if (searchResult.getScore() < MATCH_WARN_THRESHOLD) {
            log.warn("Player name '" + fuzzyName + "' matched to '" + name + "' with low score of " + searchResult.getScore());
        }

        return playersByName.get(name);
    }

    private boolean startsWithIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().startsWith(needle.toLowerCase());
    }

    private String cleanString(String source) {
        return source.replaceAll("\\s+", " ").trim();
    }
}

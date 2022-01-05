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
    private final AchievementAdditionsProvider achievementAdditionsProvider;

    public List<Achievement> parse(List<Message> messages, List<Player> players) {
        Map<String, Player> playersByName = players.stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));

        playerRenamer.addRenames(playersByName, true);

        if (!messages.isEmpty()) {
            messages.remove(0);
        }

        Map<String, List<Player>> achievementAdditions = achievementAdditionsProvider.getAdditions().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap(name -> {
                                    Player player = playersByName.get(name);
                                    if (player == null) {
                                        log.warn("Missing player in achievement additions: {}", name);
                                    }

                                    return Optional.ofNullable(player).stream();
                                })
                                .collect(Collectors.toList())
                ));

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
                playersCompleted.addAll(parseDoneLine(lastLine, playersByName));
                doneLineUsed = true;
            }

            if (achievementAdditions.containsKey(name)) {
                for (Player player : achievementAdditions.get(name)) {
                    if (playersCompleted.contains(player)) {
                        log.warn("Redundant addition of player {} to achievement {}", player.getName(), name);
                    } else {
                        playersCompleted.add(player);
                    }
                }
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

        for (String additionName : achievementAdditions.keySet()) {
            if (achievements.stream().noneMatch(achievement -> achievement.getName().equals(additionName))) {
                log.warn("Achievement addition for non-existent achievement: {}", additionName);
            }
        }

        achievements.sort(Comparator.comparing(Achievement::getName));
        return achievements;
    }

    private List<Player> parseDoneLine(String line, Map<String, Player> playersByName) {
        String[] headerAndBody = line.split(":");

        if (headerAndBody.length != 2) {
            return List.of();
        }

        String playerList = headerAndBody[1];
        String[] playerNamesRaw = playerList.split(",");

        List<Player> result = new ArrayList<>();

        for (String playerName : playerNamesRaw) {
            playerName = cleanString(playerName);
            Player player = playersByName.get(playerName);

            if (player != null) {
                result.add(player);
            }
        }

        return result;
    }

    private boolean startsWithIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().startsWith(needle.toLowerCase());
    }

    private String cleanString(String source) {
        return source.replaceAll("\\s+", " ").trim();
    }
}

package org.astropeci.urmwstats.ingestion;

import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.Tourney;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class TourneyParser {

    private static final Pattern PLACING_REGEX = Pattern.compile("[:&] ?(?<name>[^\\s&]+)");
    private static final int MATCH_WARN_THRESHOLD = 75;

    private int id = 0;

    public Tourney parseTourney(Message message, Map<String, Player> playersByName) {
        String content = message.getContentRaw();
        if (!content.startsWith("**Result**")) {
            throw new HistoryParser.ParseException();
        }

        List<String> lines = content.lines().collect(Collectors.toList());
        if (lines.size() < 4) {
            log.warn("Tourney message did not have 4 lines, ignoring\n" + content);
            throw new HistoryParser.ParseException();
        }

        Set<String> firstFuzzyNames = parsePlacing(lines.get(1));
        Set<String> secondFuzzyNames = parsePlacing(lines.get(2));
        Set<String> thirdFuzzyNames = parsePlacing(lines.get(3));

        Set<Player> first = mapSet(firstFuzzyNames, fuzzyName -> lookupPlayer(fuzzyName, playersByName));
        Set<Player> second = mapSet(secondFuzzyNames, fuzzyName -> lookupPlayer(fuzzyName, playersByName));
        Set<Player> third = mapSet(thirdFuzzyNames, fuzzyName -> lookupPlayer(fuzzyName, playersByName));

        first.forEach(player -> player.setTimesPlacedFirst(player.getTimesPlacedFirst() + 1));
        second.forEach(player -> player.setTimesPlacedSecond(player.getTimesPlacedSecond() + 1));
        third.forEach(player -> player.setTimesPlacedThird(player.getTimesPlacedThird() + 1));

        for (Player player : first) {
            player.setFractionalTourneyWins(player.getFractionalTourneyWins() + 1.0 / first.size());
        }

        Set<String> firstNames = mapSet(first, Player::getName);
        Set<String> secondNames = mapSet(second, Player::getName);
        Set<String> thirdNames = mapSet(third, Player::getName);

        return new Tourney(
                id++,
                firstNames, secondNames, thirdNames,
                message.getTimeCreated().toInstant()
        );
    }

    private Player lookupPlayer(String fuzzyName, Map<String, Player> playersByName) {
        if (playersByName.size() == 0) {
            log.warn("Tourney messages cannot be parsed when no players exist");
            throw new HistoryParser.ParseException();
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

    private Set<String> parsePlacing(String placing) {
        Set<String> fuzzyNames = new HashSet<>();

        Matcher matcher = PLACING_REGEX.matcher(placing);
        while (matcher.find()) {
            fuzzyNames.add(matcher.group("name"));
        }

        return fuzzyNames;
    }

    private <T, U> Set<U> mapSet(Set<T> set, Function<T, U> mapper) {
        return set.stream().map(mapper).collect(Collectors.toSet());
    }
}

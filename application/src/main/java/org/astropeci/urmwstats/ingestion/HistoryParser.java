package org.astropeci.urmwstats.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.data.Match;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.Tourney;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryParser {

    private final PlayerRenamer playerRenamer;

    @Value
    public static class History {
        List<Match> matches;
        List<Tourney> tourneys;
    }

    public static class ParseException extends RuntimeException { }

    public History parse(List<Message> messages, List<Player> players) {
        MatchParser matchParser = new MatchParser();
        TourneyParser tourneyParser = new TourneyParser();

        Map<String, Player> playersByName = players.stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));

        playerRenamer.addRenames(playersByName);

        List<Match> matches = new ArrayList<>();
        List<Tourney> tourneys = new ArrayList<>();

        for (Message message : messages) {
            try {
                Match match = matchParser.parseMatch(message, playersByName);
                matches.add(match);
                continue;
            } catch (ParseException ignored) { }

            try {
                Tourney tourney = tourneyParser.parseTourney(message, playersByName);
                tourneys.add(tourney);
            } catch (ParseException ignored) { }
        }

        return new History(matches, tourneys);
    }
}

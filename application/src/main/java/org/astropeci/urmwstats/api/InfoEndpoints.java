package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.astropeci.urmwstats.TrueskillSettings;
import org.astropeci.urmwstats.data.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InfoEndpoints {

    private final RepositoryCoordinator repositoryCoordinator;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TourneyRepository tourneyRepository;
    private final AchievementRepository achievementRepository;

    @Value
    private static class Info {
        Instant lastUpdated;
        int playerCount;
        int matchCount;
        int tourneyCount;
        int achievementCount;
        TrueskillSettings trueskillSettings;
    }

    @Value
    private static class StandardData {
        Info info;
        List<Player> players;
        List<Achievement> achievements;
        Tourney lastTourney;
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(
                repositoryCoordinator.getLastUpdated(),
                playerRepository.size(),
                matchRepository.size(),
                tourneyRepository.size(),
                achievementRepository.size(),
                new TrueskillSettings()
        );
    }

    @GetMapping("/standard-data")
    public StandardData data() {
        Optional<Tourney> lastTourney = tourneyRepository.mostRecent(1, null).stream().findFirst();
        if (lastTourney.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        return new StandardData(
                info(),
                playerRepository.byRanking(),
                achievementRepository.byName(),
                lastTourney.get()
        );
    }
}

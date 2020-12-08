package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@RequiredArgsConstructor
public class RepositoryCoordinator {

    public static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TourneyRepository tourneyRepository;
    private final AchievementRepository achievementRepository;

    @Getter
    private volatile Instant lastUpdated = Instant.EPOCH;

    public void update(List<Player> players, List<Match> matches, List<Tourney> tourneys, List<Achievement> achievements) {
        @Cleanup("unlock") Lock lock = LOCK.writeLock();
        lock.lock();

        playerRepository.update(players);
        matchRepository.update(matches);
        tourneyRepository.update(tourneys);
        achievementRepository.update(achievements);

        lastUpdated = Instant.now();
    }
}

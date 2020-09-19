package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import lombok.Synchronized;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Component
public class PlayerRepository {

    private volatile List<Player> players = new ArrayList<>();

    public List<Player> getPlayersByRanking() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return new ArrayList<>(players);
    }

    public int size() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return players.size();
    }

    @Synchronized
    /* package-private */ void update(List<Player> players) {
        this.players = players;
    }
}

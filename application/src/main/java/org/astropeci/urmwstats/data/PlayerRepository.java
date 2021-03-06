package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Component
public class PlayerRepository {

    private volatile List<Player> players = new ArrayList<>();

    public List<Player> byRanking() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return new ArrayList<>(players);
    }

    public int size() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return players.size();
    }

    /* package-private */ void update(List<Player> players) {
        this.players = players;
    }
}

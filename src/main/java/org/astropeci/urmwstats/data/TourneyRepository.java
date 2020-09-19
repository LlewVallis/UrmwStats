package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Component
public class TourneyRepository {

    private volatile List<Tourney> tourneys = new ArrayList<>();

    public List<Tourney> mostRecent(int count, @Nullable String filter) {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        List<Tourney> results = new ArrayList<>();
        for (int i = tourneys.size() - 1; i >= 0 && results.size() < count; i--) {
            Tourney tourney = tourneys.get(i);
            if (filterTourney(tourney, filter)) {
                results.add(tourney);
            }
        }

        return results;
    }

    private boolean filterTourney(Tourney tourney, @Nullable String filter) {
        if (filter == null) {
            return true;
        } else {
            return tourney.getFirst().contains(filter) ||
                    tourney.getSecond().contains(filter) ||
                    tourney.getThird().contains(filter);
        }
    }

    public int size() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return tourneys.size();
    }

    /* package-private */ void update(List<Tourney> tourneys) {
        this.tourneys = tourneys;
    }
}

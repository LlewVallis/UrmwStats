package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Component
public class MatchRepository {

    private volatile List<Match> matches = new ArrayList<>();

    public List<Match> mostRecent(int count, @Nullable String filter) {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        List<Match> results = new ArrayList<>();
        for (int i = matches.size() - 1; i >= 0 && results.size() < count; i--) {
            Match match = matches.get(i);
            if (filterMatch(match, filter)) {
                results.add(match);
            }
        }

        return results;
    }

    private boolean filterMatch(Match match, @Nullable String filter) {
        if (filter == null) {
            return true;
        } else {
            return match.getParticipants().stream()
                    .anyMatch(participant -> participant.getName().equals(filter));
        }
    }

    public int size() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return matches.size();
    }

    /* package-private */ void update(List<Match> matches) {
        this.matches = matches;
    }
}

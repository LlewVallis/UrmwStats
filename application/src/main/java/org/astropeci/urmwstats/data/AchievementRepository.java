package org.astropeci.urmwstats.data;

import lombok.Cleanup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Component
public class AchievementRepository {

    private volatile List<Achievement> achievements = new ArrayList<>();

    public List<Achievement> byName() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return new ArrayList<>(achievements);
    }

    public int size() {
        @Cleanup("unlock") Lock lock = RepositoryCoordinator.LOCK.readLock();
        lock.lock();

        return achievements.size();
    }

    /* package-private */ void update(List<Achievement> achievements) {
        this.achievements = achievements;
    }
}

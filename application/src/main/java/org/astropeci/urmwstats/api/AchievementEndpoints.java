package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.data.Achievement;
import org.astropeci.urmwstats.data.AchievementRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AchievementEndpoints {

    private final AchievementRepository achievementRepository;

    @GetMapping("/achievements")
    public List<Achievement> achievements() {
        return achievementRepository.byName();
    }
}

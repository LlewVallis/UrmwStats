package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.data.Tourney;
import org.astropeci.urmwstats.data.TourneyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TourneyEndpoints {

    private final TourneyRepository tourneyRepository;

    @GetMapping("/tourneys/recent")
    public List<Tourney> recent(@RequestParam int count, @RequestParam(required = false) String filter) {
        if (count < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count cannot be less than zero");
        }

        return tourneyRepository.mostRecent(count, filter);
    }
}

package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.data.Match;
import org.astropeci.urmwstats.data.MatchRepository;
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
public class MatchEndpoints {

    private final MatchRepository matchRepository;

    @GetMapping("/matches/recent")
    public List<Match> recent(@RequestParam int count, @RequestParam(required = false) String filter) {
        if (count < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count cannot be less than zero");
        }

        return matchRepository.mostRecent(count, filter);
    }
}

package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.PlayerRepository;
import org.astropeci.urmwstats.ingestion.PlayerRenamer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlayerEndpoints {

    private final PlayerRepository playerRepository;
    private final PlayerRenamer playerRenamer;

    @GetMapping("/players")
    public List<Player> players() {
        return playerRepository.byRanking();
    }

    @GetMapping("/renames")
    public Map<String, String> renames() {
        return playerRenamer.getRenames();
    }
}

package org.astropeci.urmwstats.ingestion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.astropeci.urmwstats.data.Player;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Component
public class PlayerRenamer {

    private Map<String, String> renames;

    public void addRenames(Map<String, Player> playersByName) {
        getRenames().forEach((oldName, newName) -> {
            Player player = playersByName.get(newName);

            if (player == null) {
                log.warn("Renaming of " + oldName + " to " + newName + " could not be completed since " + newName + " did not exist");
                return;
            }

            playersByName.put(oldName, player);
        });
    }

    @SneakyThrows({ IOException.class })
    public Map<String, String> getRenames() {
        if (renames == null) {
            ObjectMapper mapper = new ObjectMapper();
            URL renameFile = getClass().getResource("/player-renames.json");
            renames = mapper.readValue(renameFile, new TypeReference<>() { });
        }

        return renames;
    }
}

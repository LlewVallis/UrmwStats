package org.astropeci.urmwstats.ingestion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Component
public class AchievementAdditionsProvider {

    private Map<String, List<String>> additions;

    @SneakyThrows({ IOException.class })
    public Map<String, List<String>> getAdditions() {
        if (additions == null) {
            ObjectMapper mapper = new ObjectMapper();
            URL renameFile = getClass().getResource("/achievement-additions.json");
            additions = mapper.readValue(renameFile, new TypeReference<>() { });
        }

        return additions;
    }
}

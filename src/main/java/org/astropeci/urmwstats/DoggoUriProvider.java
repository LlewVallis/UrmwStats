package org.astropeci.urmwstats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DoggoUriProvider {

    private List<String> uris = null;

    @SneakyThrows({ IOException.class, URISyntaxException.class })
    public URI randomUri() {
        if (uris == null) {
            ObjectMapper mapper = new ObjectMapper();
            URL resource = getClass().getResource("/doggo-urls.json");
            uris = mapper.readValue(resource, new TypeReference<>() { });
        }

        int index = ThreadLocalRandom.current().nextInt(uris.size());
        return new URL(uris.get(index)).toURI();
    }
}

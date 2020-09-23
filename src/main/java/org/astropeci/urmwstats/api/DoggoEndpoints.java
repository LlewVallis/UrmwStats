package org.astropeci.urmwstats.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequestMapping("/api")
public class DoggoEndpoints {

    private List<String> uris = null;

    @GetMapping("/doggos/random")
    public ResponseEntity<Resource> randomDoggo() {
        URI uri = randomUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = new HttpEntity<>("");

        try {
            ResponseEntity<Resource> rawResponse = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Resource.class);

            HttpHeaders headers = HttpHeaders.writableHttpHeaders(rawResponse.getHeaders());
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(
                    rawResponse.getBody(),
                    headers,
                    rawResponse.getStatusCode()
            );
        } catch (HttpStatusCodeException e) {
            log.warn("Failed to fetch image from " + uri, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @SneakyThrows({ IOException.class, URISyntaxException.class })
    private URI randomUri() {
        if (uris == null) {
            ObjectMapper mapper = new ObjectMapper();
            URL resource = getClass().getResource("/doggo-urls.json");
            uris = mapper.readValue(resource, new TypeReference<>() { });
        }

        int index = ThreadLocalRandom.current().nextInt(uris.size());
        return new URL(uris.get(index)).toURI();
    }
}

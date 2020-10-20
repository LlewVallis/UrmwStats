package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.astropeci.urmwstats.DoggoUriProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoggoEndpoints {

    private final DoggoUriProvider doggoUriProvider;

    @GetMapping("/doggos/random")
    public ResponseEntity<Resource> randomDoggo() {
        URI uri = doggoUriProvider.randomUri();

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
}

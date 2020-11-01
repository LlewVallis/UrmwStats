package org.astropeci.urmwstats.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.astropeci.urmwstats.DoggoProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoggoEndpoints {

    private final DoggoProvider doggoProvider;

    @GetMapping("/doggos/random")
    public ResponseEntity<byte[]> randomDoggo() {
        return new ResponseEntity<>(
                doggoProvider.randomDoggo(),
                new LinkedMultiValueMap<>(Headers.of(
                        "Content-Type", "image/jpeg"
                ).toMultimap()),
                HttpStatus.OK
        );
    }
}

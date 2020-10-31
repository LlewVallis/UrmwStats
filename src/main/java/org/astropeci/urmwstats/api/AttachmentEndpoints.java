package org.astropeci.urmwstats.api;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class AttachmentEndpoints {

    @SneakyThrows({ IOException.class })
    @GetMapping("/download-attachment/{channelId}/{attachmentId}/{fileName}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable String channelId,
            @PathVariable String attachmentId,
            @PathVariable String fileName
    ) {
        URL url = new URL(String.format(
                "https://cdn.discordapp.com/attachments/%s/%s/%s",
                URLEncoder.encode(channelId, StandardCharsets.UTF_8),
                URLEncoder.encode(attachmentId, StandardCharsets.UTF_8),
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        ));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        @Cleanup Response upstreamResponse = client.newCall(request).execute();
        if (upstreamResponse.code() != 200 || upstreamResponse.body() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                upstreamResponse.body().bytes(),
                new LinkedMultiValueMap<>(
                        Headers.of("Content-Type", "application/octet-stream").toMultimap()
                ),
                HttpStatus.OK
        );
    }
}

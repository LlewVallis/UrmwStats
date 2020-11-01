package org.astropeci.urmwstats;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipInputStream;

@Component
public class DoggoProvider {

    public List<byte[]> doggos = null;

    public byte[] randomDoggo() {
        if (doggos == null) {
            populateDoggos();
        }

        byte[] doggo = doggos.remove(0);

        int size = doggos.size();
        int insertSafeRange = size / 2;
        int newIndex = new Random().nextInt(size - insertSafeRange) + insertSafeRange;

        doggos.add(newIndex, doggo);

        return doggo;
    }

    @SneakyThrows({ IOException.class })
    private void populateDoggos() {
        doggos = new ArrayList<>();

        InputStream input = getClass().getResourceAsStream("/doggos.zip");
        @Cleanup ZipInputStream zipInput = new ZipInputStream(input, StandardCharsets.UTF_8);

        while (zipInput.getNextEntry() != null) {
            doggos.add(zipInput.readAllBytes());
        }

        Collections.shuffle(doggos);
    }
}

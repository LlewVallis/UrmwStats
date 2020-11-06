package org.astropeci.urmwstats.recording;

import com.cloudburst.lame.lowlevel.LameEncoder;
import com.cloudburst.lame.mp3.Lame;
import com.cloudburst.lame.mp3.MPEGMode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Slf4j
public class Mp3ChunkStream extends OutputStream implements AutoCloseable {

    private static final int INPUT_BUFFER_SIZE = 512 * 1024;
    private static final int MAX_CHUNK_SIZE = 8 * 1024 * 1024 - 1024;

    private final List<byte[]> packedChunks = new ArrayList<>();
    private final List<byte[]> rawChunks = new ArrayList<>();
    private ByteArrayOutputStream inputBuffer = newInputBuffer();
    private boolean closed = false;

    private final LameEncoder encoder = new LameEncoder(
            AudioReceiveHandler.OUTPUT_FORMAT,
            LameEncoder.BITRATE_AUTO,
            MPEGMode.MONO,
            Lame.QUALITY_LOWEST,
            true
    );

    @Override
    public void write(int b) {
        if (closed) {
            return;
        }

        inputBuffer.write(b);
        afterWrite();
    }

    @Override
    @SneakyThrows({ IOException.class })
    public void write(byte[] b) {
        if (closed) {
            return;
        }

        inputBuffer.write(b);
        afterWrite();
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (closed) {
            return;
        }

        inputBuffer.write(b, off, len);
        afterWrite();
    }

    private void afterWrite() {
        if (inputBuffer.size() >= INPUT_BUFFER_SIZE) {
            drainInputBuffer();
        }

        partiallyPackRawChunks();
    }

    private void partiallyPackRawChunks() {
        while (rawChunks.stream().mapToInt(chunk -> chunk.length).sum() >= MAX_CHUNK_SIZE) {
            packNextRawChunk();
        }
    }

    @SneakyThrows({ IOException.class })
    private void packNextRawChunk() {
        log.info("Packing chunk");
        ByteArrayOutputStream currentChunk = new ByteArrayOutputStream();

        Iterator<byte[]> iterator = rawChunks.iterator();
        while (iterator.hasNext()) {
            byte[] rawChunk = iterator.next();
            if (currentChunk.size() + rawChunk.length > MAX_CHUNK_SIZE) {
                break;
            }

            currentChunk.write(rawChunk);
            iterator.remove();
        }

        packedChunks.add(currentChunk.toByteArray());
    }

    private void drainInputBuffer() {
        byte[] input = inputBuffer.toByteArray();
        byte[] buffer = new byte[encoder.getMP3BufferSize()];

        int bytesWritten;
        int bytesToTransfer = Math.min(buffer.length, input.length);
        int currentPcmPosition = 0;

        while ((bytesWritten = encoder.encodeBuffer(input, currentPcmPosition, bytesToTransfer, buffer)) > 0) {
            currentPcmPosition += bytesToTransfer;
            bytesToTransfer = Math.min(buffer.length, input.length - currentPcmPosition);

            byte[] rawChunk = Arrays.copyOf(buffer, bytesWritten);
            rawChunks.add(rawChunk);
        }

        inputBuffer = newInputBuffer();
    }

    public byte[] takeChunk() {
        if (packedChunks.size() > 0) {
            log.info("Removing packed chunk");
            return packedChunks.remove(0);
        } else {
            return null;
        }
    }

    private ByteArrayOutputStream newInputBuffer() {
        return new ByteArrayOutputStream();
    }

    @Override
    public void flush() {
        log.info("Flushing MP3 stream");
        drainInputBuffer();

        while (!rawChunks.isEmpty()) {
            packNextRawChunk();
        }
    }

    @Override
    public void close() {
        log.info("Closing MP3 stream");
        closed = true;
        flush();
        encoder.close();
    }
}

package org.astropeci.urmwstats.recording;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ActiveRecording implements AudioReceiveHandler, ConnectionListener, AutoCloseable {

    private static final int MAX_FRAME_BACKLOG = 200;

    @Getter
    private final MessageChannel commandChannel;
    private final AudioManager manager;
    private final Consumer<byte[]> chunkHandler;
    private final Runnable interruptHandler;
    private final Consumer<Integer> silenceHandler;

    private final Mp3ChunkStream chunkStream = new Mp3ChunkStream();
    private final ExecutorService chunkStreamExecutor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_FRAME_BACKLOG)
    );

    private int silentFrames = 0;

    public ActiveRecording(
            VoiceChannel channel,
            MessageChannel commandChannel,
            Consumer<byte[]> chunkHandler,
            Runnable interruptHandler,
            Consumer<Integer> silenceHandler
    ) {
        this.commandChannel = commandChannel;
        this.chunkHandler = chunkHandler;
        this.interruptHandler = interruptHandler;
        this.silenceHandler = silenceHandler;

        manager = channel.getGuild().getAudioManager();
        manager.openAudioConnection(channel);
        manager.setReceivingHandler(this);
        manager.setConnectionListener(this);
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        byte[] audioData = combinedAudio.getAudioData(1);

        if (isZero(audioData)) {
            silenceHandler.accept(++silentFrames);
        } else {
            silentFrames = 0;
        }

        try {
            chunkStreamExecutor.submit(() -> {
                synchronized (chunkStream) {
                    chunkStream.write(audioData);

                    byte[] chunk = takeChunk();
                    if (chunk != null) {
                        chunkHandler.accept(chunk);
                    }
                }
            });
        } catch (RejectedExecutionException ignored) { }
    }

    public byte[] takeChunk() {
        synchronized (chunkStream) {
            return chunkStream.takeChunk();
        }
    }

    private boolean isZero(byte[] value) {
        for (byte b : value) {
            if (b != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onStatusChange(@Nonnull ConnectionStatus status) {
        if (status != ConnectionStatus.NOT_CONNECTED && !status.shouldReconnect()) {
            interruptHandler.run();
        }
    }

    @Override
    @Synchronized
    @SneakyThrows({ InterruptedException.class })
    public void close() {
        chunkStreamExecutor.shutdown();
        chunkStreamExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        synchronized (chunkStream) {
            manager.closeAudioConnection();
            chunkStream.close();
        }
    }

    @Override
    public void onPing(long ping) { }

    @Override
    public void onUserSpeaking(@Nonnull User user, boolean speaking) { }
}

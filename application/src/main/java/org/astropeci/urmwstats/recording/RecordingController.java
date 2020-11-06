package org.astropeci.urmwstats.recording;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class RecordingController implements DisposableBean {

    private static final int MAX_SILENT_FRAMES = 50 * 60 * 10;
    private ActiveRecording activeRecording = null;

    @Synchronized
    public void start(VoiceChannel channel, MessageReceivedEvent event) {
        if (activeRecording != null) {
            throw new AlreadyRecordingException();
        }

        log.info("Starting recording in {}", channel.getName());
        event.getChannel().sendMessage("🎥 Started recording").queue();
        activeRecording = new ActiveRecording(channel, event.getChannel(), this::onChunk, this::onInterrupt, this::onSilence);
    }

    @Synchronized
    public void save(MessageReceivedEvent event) {
        if (activeRecording == null) {
            throw new NotRecordingException();
        }

        log.info("Saving recording");
        event.getChannel().sendMessage("⬆️ Uploading recording").complete();

        activeRecording.close();
        byte[] finalChunk = activeRecording.takeChunk();

        if (finalChunk == null) {
            log.warn("No recording final chunk");
            finalChunk = new byte[0];
        }

        activeRecording.getCommandChannel()
                .sendMessage("💾 Recording finished")
                .addFile(finalChunk, "recording.mp3")
                .complete();

        if (!activeRecording.getCommandChannel().equals(event.getChannel())) {
            event.getChannel().sendMessageFormat(
                    "👍 Uploaded to <#%s>",
                    activeRecording.getCommandChannel().getId()
            ).complete();
        }

        activeRecording = null;
    }

    @Synchronized
    public void discard(MessageReceivedEvent event) {
        if (activeRecording == null) {
            throw new NotRecordingException();
        }

        log.info("Discarding recording");
        activeRecording.close();

        event.getChannel().sendMessage("🗑️ Discarded recording").complete();

        if (!activeRecording.getCommandChannel().equals(event.getChannel())) {
            activeRecording.getCommandChannel().sendMessageFormat(
                    "🗑️ Recording was discarded in <#%s>",
                    event.getChannel().getId()
            ).complete();
        }

        activeRecording = null;
    }

    @Synchronized
    private void onChunk(byte[] chunk) {
        log.info("Uploading intermediary chunk of size {}", chunk.length);
        activeRecording.getCommandChannel()
                .sendMessage("💾 Recording has reached 8MB, restarting")
                .addFile(chunk, "recording.mp3")
                .queue();
    }

    @Synchronized
    private void onInterrupt() {
        if (activeRecording != null) {
            log.info("Recording interrupted");
            activeRecording.getCommandChannel().sendMessage("⬆️ Connection interrupted, uploading recording").queue();

            activeRecording.close();
            byte[] finalChunk = activeRecording.takeChunk();

            if (finalChunk == null) {
                log.warn("No recording final chunk");
                finalChunk = new byte[0];
            }

            activeRecording.getCommandChannel()
                    .sendMessage("💾 Recording finished")
                    .addFile(finalChunk, "recording.mp3")
                    .complete();

            activeRecording = null;
        }
    }

    private void onSilence(int frames) {
        if (frames == MAX_SILENT_FRAMES) {
            CompletableFuture.runAsync(this::cancelDueToInactivity).exceptionally(e -> {
                log.error("Failed to cancel due to inactivity", e);
                return null;
            });
        }
    }

    @Synchronized
    private void cancelDueToInactivity() {
        log.info("Cancelling recording due to inactivity");
        activeRecording.getCommandChannel().sendMessage("⬆️ No one has spoken for 10 minutes, uploading recording").complete();

        activeRecording.close();
        byte[] finalChunk = activeRecording.takeChunk();

        if (finalChunk == null) {
            log.warn("No recording final chunk");
            finalChunk = new byte[0];
        }

        activeRecording.getCommandChannel()
                .sendMessage("💾 Recording finished")
                .addFile(finalChunk, "recording.mp3")
                .complete();

        activeRecording = null;
    }

    @Override
    @Synchronized
    public void destroy() {
        if (activeRecording != null) {
            log.warn("Discarding recording due to shutdown");
            activeRecording.close();
        }
    }
}

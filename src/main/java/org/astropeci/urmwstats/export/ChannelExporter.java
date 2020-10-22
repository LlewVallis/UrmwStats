package org.astropeci.urmwstats.export;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
public class ChannelExporter {

    @Value
    public static class Result {
        byte[] content;
        int messageCount;
        int attachmentsSkipped;
        int attachmentsDownloaded;
        long attachmentSpaceCompressed;
        long attachmentSpaceUncompressed;
    }

    @SneakyThrows({ JsonProcessingException.class, IOException.class })
    public Result createExport(MessageChannel channel, Consumer<Integer> statusUpdate) {
        log.info("Starting channel export for #{}", channel.getName());

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        GZIPOutputStream compressedOutput = new GZIPOutputStream(byteOutput);
        JsonGenerator json = new JsonFactory().createGenerator(compressedOutput);

        json.writeStartArray();

        AtomicInteger messageCount = new AtomicInteger();
        AtomicInteger attachmentsSkipped = new AtomicInteger();
        AtomicInteger attachmentsDownloaded = new AtomicInteger();
        AtomicLong attachmentSpaceCompressed = new AtomicLong();
        AtomicLong attachmentSpaceUncompressed = new AtomicLong();

        channel.getIterableHistory()
                .forEachRemaining(message -> {
                    try {
                        json.writeStartObject();

                        json.writeStringField("content", message.getContentRaw());
                        json.writeNumberField("author", message.getAuthor().getIdLong());
                        json.writeNumberField("timestamp", message.getTimeCreated().toInstant().toEpochMilli());

                        json.writeArrayFieldStart("reactions");
                        for (MessageReaction reaction: message.getReactions()) {
                            json.writeStartObject();
                            json.writeStringField("name", reaction.getReactionEmote().getName());
                            json.writeNumberField("count", reaction.getCount());
                            json.writeEndObject();
                        }
                        json.writeEndArray();

                        json.writeArrayFieldStart("attachments");

                        if (message.isPinned() || message.getReactionByUnicode("📌") != null) {
                            for (Message.Attachment attachment : message.getAttachments()) {
                                log.info(
                                        "Downloading {} at {} from {}",
                                        attachment.getFileName(),
                                        attachment.getUrl(),
                                        message.getJumpUrl()
                                );

                                byte[] content = attachment.retrieveInputStream().join().readAllBytes();

                                ByteArrayOutputStream compressedContentOut = new ByteArrayOutputStream();
                                new GZIPOutputStream(compressedContentOut).write(content);
                                byte[] compressedContent = compressedContentOut.toByteArray();

                                json.writeStartObject();
                                json.writeStringField("name", attachment.getFileName());
                                json.writeFieldName("content");
                                json.writeBinary(compressedContent);
                                json.writeEndObject();

                                attachmentSpaceUncompressed.addAndGet(content.length);
                                attachmentSpaceCompressed.addAndGet(compressedContent.length);

                                attachmentsDownloaded.incrementAndGet();
                            }
                        } else {
                            attachmentsSkipped.addAndGet(message.getAttachments().size());
                        }

                        json.writeEndArray();

                        json.writeEndObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    statusUpdate.accept(messageCount.incrementAndGet());
                    return true;
                });

        json.writeEndArray();
        json.close();

        byte[] content = byteOutput.toByteArray();

        log.info("Completed export and serialization of #{}", channel.getName());
        return new Result(
                content,
                messageCount.get(),
                attachmentsSkipped.get(),
                attachmentsDownloaded.get(),
                attachmentSpaceCompressed.get(),
                attachmentSpaceUncompressed.get()
        );
    }
}

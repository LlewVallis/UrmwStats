package org.astropeci.urmwstats.export;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelExporter {

    private final JDA jda;

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

        JsonGenerator json = new JsonFactory()
                .setCodec(new ObjectMapper())
                .createGenerator(compressedOutput);

        json.writeStartObject();
        json.writeNumberField("formatVersion", 1);
        json.writeStringField("channelName", channel.getName());
        json.writeStringField("timestamp", Instant.now().toString());

        json.writeArrayFieldStart("messages");

        AtomicInteger messageCount = new AtomicInteger();
        AtomicInteger attachmentsSkipped = new AtomicInteger();
        AtomicInteger attachmentsDownloaded = new AtomicInteger();
        AtomicLong attachmentSpaceCompressed = new AtomicLong();
        AtomicLong attachmentSpaceUncompressed = new AtomicLong();

        Map<String, String> authorNames = new HashMap<>();

        Guild guild = null;
        if (channel instanceof TextChannel) {
            guild = ((TextChannel) channel).getGuild();
        }

        UserCache userCache = new UserCache(jda);
        MemberCache memberCache = new MemberCache(guild);
        EmbedTextProcessor etp = new EmbedTextProcessor(jda, userCache, memberCache);

        channel.getIterableHistory()
                .forEachRemaining(message -> {
                    writeMessage(
                            message,
                            json,
                            authorNames,
                            userCache,
                            memberCache,
                            etp,
                            attachmentsSkipped,
                            attachmentsDownloaded,
                            attachmentSpaceCompressed,
                            attachmentSpaceUncompressed
                    );

                    statusUpdate.accept(messageCount.incrementAndGet());
                    return true;
                });

        json.writeEndArray();

        json.writeObjectField("authorNames", authorNames);

        json.writeEndObject();
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

    @SneakyThrows({ IOException.class })
    private void writeMessage(
            Message message,
            JsonGenerator json,
            Map<String, String> authorNames,
            UserCache userCache,
            MemberCache memberCache,
            EmbedTextProcessor etp,
            AtomicInteger attachmentsSkipped,
            AtomicInteger attachmentsDownloaded,
            AtomicLong attachmentSpaceCompressed,
            AtomicLong attachmentSpaceUncompressed
    ) {
        json.writeStartObject();

        writeMessageInfo(message, json, authorNames, userCache, memberCache);
        writeReactions(message, json);
        writeAttachments(
                message,
                json,
                attachmentsSkipped,
                attachmentsDownloaded,
                attachmentSpaceCompressed,
                attachmentSpaceUncompressed
        );
        writeEmbeds(message, json, etp);

        json.writeEndObject();
    }

    @SneakyThrows({ IOException.class })
    private void writeMessageInfo(
            Message message,
            JsonGenerator json,
            Map<String, String> authorNames,
            UserCache userCache,
            MemberCache memberCache
    ) {
        int flags = message.getFlags().stream()
                .mapToInt(Message.MessageFlag::getValue)
                .sum();

        int type = message.getType().getId();

        String authorId = message.getAuthor().getId();
        authorNames.computeIfAbsent(authorId, _authorId -> {
            long id = message.getAuthor().getIdLong();

            Member member = memberCache.retrieve(id);
            if (member != null) {
                return member.getEffectiveName();
            }

            User user = userCache.retrieve(id);
            if (user != null) {
                return user.getName();
            }

            return null;
        });

        json.writeNumberField("flags", flags);
        json.writeNumberField("type", type);
        json.writeStringField("content", message.getContentDisplay());
        json.writeStringField("author", authorId);
        json.writeStringField("timestamp", message.getTimeCreated().toInstant().toString());
    }

    @SneakyThrows({ IOException.class })
    private void writeReactions(Message message, JsonGenerator json) {
        json.writeArrayFieldStart("reactions");
        for (MessageReaction reaction: message.getReactions()) {
            json.writeStartObject();
            json.writeStringField("name", reaction.getReactionEmote().getName());
            json.writeNumberField("count", reaction.getCount());
            json.writeEndObject();
        }
        json.writeEndArray();
    }

    @SneakyThrows({ IOException.class })
    private void writeAttachments(
            Message message,
            JsonGenerator json,
            AtomicInteger attachmentsSkipped,
            AtomicInteger attachmentsDownloaded,
            AtomicLong attachmentSpaceCompressed,
            AtomicLong attachmentSpaceUncompressed
    ) {
        json.writeArrayFieldStart("attachments");

        boolean saveAttachments = message.isPinned() || message.getReactionByUnicode("📌") != null;
        for (Message.Attachment attachment : message.getAttachments()) {
            json.writeStartObject();

            if (saveAttachments) {
                log.info(
                        "Downloading {} at {} from {}",
                        attachment.getFileName(),
                        attachment.getUrl(),
                        message.getJumpUrl()
                );

                byte[] content = attachment.retrieveInputStream().join().readAllBytes();

                ByteArrayOutputStream compressedContentOut = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(compressedContentOut);
                gzipOut.write(content);
                gzipOut.close();

                byte[] compressedContent = compressedContentOut.toByteArray();

                json.writeStringField("name", attachment.getFileName());
                json.writeFieldName("content");
                json.writeBinary(Base64Variants.MIME, compressedContent, 0, compressedContent.length);

                attachmentSpaceUncompressed.addAndGet(content.length);
                attachmentSpaceCompressed.addAndGet(compressedContent.length);

                attachmentsDownloaded.incrementAndGet();
            } else {
                json.writeStringField("name", attachment.getFileName());
                json.writeNullField("content");

                attachmentsSkipped.incrementAndGet();
            }

            json.writeEndObject();
        }

        json.writeEndArray();
    }

    @SneakyThrows({ IOException.class })
    private void writeEmbeds(Message message, JsonGenerator json, EmbedTextProcessor etp) {
        json.writeArrayFieldStart("embeds");

        for (MessageEmbed embed : message.getEmbeds()) {
            json.writeStartObject();

            if (embed.getAuthor() != null) {
                json.writeObjectField("author", etp.process(embed.getAuthor().getName()));
            } else {
                json.writeNullField("author");
            }

            if (embed.getFooter() != null) {
                json.writeObjectField("footer", etp.process(embed.getFooter().getText()));
            } else {
                json.writeNullField("footer");
            }

            json.writeObjectField("title", etp.process(embed.getTitle()));
            json.writeObjectField("description", etp.process(embed.getDescription()));

            json.writeArrayFieldStart("fields");
            for (MessageEmbed.Field field : embed.getFields()) {
                json.writeStartObject();
                json.writeObjectField("name", etp.process(field.getName()));
                json.writeObjectField("value", etp.process(field.getValue()));
                json.writeEndObject();
            }
            json.writeEndArray();

            json.writeEndObject();
        }

        json.writeEndArray();
    }
}

package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.export.ChannelExporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportCommand implements Command {

    private final ChannelExporter channelExporter;

    @Override
    public String label() {
        return "export";
    }

    @Override
    public String usage() {
        return "export";
    }

    @Override
    public String helpDescription() {
        return "Exports a transcript of the channel";
    }

    @Override
    public int helpPriority() {
        return 2;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        String baseStatusMessage = "⚙️ Exporting channel";

        Message statusMessage = event.getChannel()
                .sendMessage(baseStatusMessage)
                .complete();

        long startTime = System.currentTimeMillis();
        AtomicLong lastUpdateTime = new AtomicLong(startTime);

        ChannelExporter.Result result = channelExporter.createExport(event.getChannel(), count -> {
            if (System.currentTimeMillis() - lastUpdateTime.get() > 1000) {
                log.info("Exported {} messages in #{}", count, event.getChannel().getName());
                lastUpdateTime.set(System.currentTimeMillis());

                statusMessage.editMessage(String.format(
                        "%s (`%s` messages exported so far)",
                        baseStatusMessage,
                        count
                )).queue();
            }
        });

        float duration = (System.currentTimeMillis() - startTime) / 1000f;
        statusMessage.editMessage(baseStatusMessage + " (done)").queue();

        EmbedBuilder successEmbed = CommandUtil.coloredEmbedBuilder()
                .setTitle(String.format(
                        "🎉 Finished export in `%.1f` seconds",
                        duration
                ))
                .addField("Messages", String.format(
                        "Exported `%s` messages from <#%s> at a rate of `%.1f` per second",
                        result.getMessageCount(),
                        event.getChannel().getId(),
                        result.getMessageCount() / duration
                ), true)
                .addField("Attachments", String.format(
                        "Downloaded `%s` and skipped `%s` attachments, using `%.2f` MB (compressed) or `%.2f` MB (uncompressed)",
                        result.getAttachmentsDownloaded(),
                        result.getAttachmentsSkipped(),
                        result.getAttachmentSpaceCompressed() / 1024f / 1024f,
                        result.getAttachmentSpaceUncompressed() / 1024f / 1024f
                ), true);

        event.getChannel().sendMessage(successEmbed.build()).queue();

        log.info("Openning DM for export file");

        PrivateChannel dm;
        try {
            dm = event.getAuthor().openPrivateChannel().complete();
        } catch (RuntimeException e) {
            log.error("Failed to open DM for export file", e);
            throw new CommandException("🚫 Could not open DM");
        }

        byte[] content = result.getContent();
        if (content.length > 8 * 1024 * 1024) {
            log.error("Export too large to send, was {} bytes", content.length);
            throw new CommandException(String.format(
                    "📚 File too large to send (`%.1f` MB)",
                    content.length / 1024f / 1024f
            ));
        }

        try {
            dm.sendFile(content, "channel-export.json.gzip").complete();
        } catch (RuntimeException e) {
            log.error("Failed to upload export file to DM", e);
            throw new CommandException("🚫 Could not send via DM");
        }

        event.getChannel().sendMessage("📨 Sent via DM").queue();
    }
}

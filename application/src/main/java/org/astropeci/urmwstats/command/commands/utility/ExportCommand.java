package org.astropeci.urmwstats.command.commands.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.export.ChannelExporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportCommand implements Command {

    private final ChannelExporter channelExporter;
    private final JDA jda;

    @Override
    public String label() {
        return "export";
    }

    @Override
    public String usage() {
        return "export <channel>";
    }

    @Override
    public String helpDescription() {
        return "Exports a transcript of the channel";
    }

    @Override
    public HelpSection section() {
        return HelpSection.UTILITY;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        MessageChannel channel = CommandUtil.parseChannel(arguments.get(0), event, jda);

        String baseStatusMessage = String.format("⚙️ Exporting <#%s>", channel.getId());

        Message statusMessage = event.getChannel()
                .sendMessage(baseStatusMessage)
                .complete();

        long startTime = System.currentTimeMillis();
        AtomicLong lastUpdateTime = new AtomicLong(startTime);

        try {
            channel.getHistory();
        } catch (InsufficientPermissionException e) {
            throw new CommandException("❌ URMW Stats doesn't have permission to that channel");
        }

        ChannelExporter.Result result = channelExporter.createExport(channel, count -> {
            if (System.currentTimeMillis() - lastUpdateTime.get() > 1000) {
                log.info("Exported {} messages in #{}", count, channel.getName());
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
                .addField("Messages", String.format(
                        "Exported `%s` messages from <#%s> at a rate of `%.1f` per second",
                        result.getMessageCount(),
                        channel.getId(),
                        result.getMessageCount() / duration
                ), true)
                .addField("Attachments", String.format(
                        "Downloaded `%s` and skipped `%s` attachments, using `%.2f` MB (compressed) or `%.2f` MB (uncompressed)",
                        result.getAttachmentsDownloaded(),
                        result.getAttachmentsSkipped(),
                        result.getAttachmentSpaceCompressed() / 1024f / 1024f,
                        result.getAttachmentSpaceUncompressed() / 1024f / 1024f
                ), true);

        byte[] content = result.getContent();
        if (content.length > 8 * 1024 * 1024) {
            log.error("Export too large to send, was {} bytes", content.length);
            throw new CommandException(String.format(
                    "📚 File too large to send (`%.1f` of 8 MB)",
                    content.length / 1024f / 1024f
            ));
        }

        String baseSuccessMessage = "🎉 Finished export in `%.1f` seconds";
        String fileName = "export.json.gz";

        Message successMessage = event.getChannel().sendMessage(String.format(
                baseSuccessMessage,
                duration
        ))
                .embed(successEmbed.build())
                .addFile(content, fileName)
                .complete();

        successMessage.editMessage(String.format(
                baseSuccessMessage + "\nhttps://urmw.markng.me/export/%s/%s/%s",
                duration,
                successMessage.getChannel().getId(),
                successMessage.getAttachments().get(0).getId(),
                fileName
        )).queue();
    }
}

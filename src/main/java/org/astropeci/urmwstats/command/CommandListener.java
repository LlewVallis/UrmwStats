package org.astropeci.urmwstats.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.astropeci.urmwstats.DoggoUriProvider;
import org.astropeci.urmwstats.SecretProvider;
import org.astropeci.urmwstats.auth.NotStaffException;
import org.astropeci.urmwstats.auth.RoleManager;
import org.astropeci.urmwstats.export.ChannelExporter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CommandListener extends ListenerAdapter {

    private final Set<String> prefixes;
    private final String testingGuildId;
    private final boolean producitonCommands;

    private final RoleManager roleManager;
    private final ChannelExporter channelExporter;
    private final DoggoUriProvider doggoUriProvider;

    public CommandListener(
            JDA jda,
            SecretProvider secretProvider,
            RoleManager roleManager,
            ChannelExporter channelExporter,
            DoggoUriProvider doggoUriProvider,
            Environment environment
    ) {
        this.roleManager = roleManager;
        this.channelExporter = channelExporter;
        this.doggoUriProvider = doggoUriProvider;

        jda.addEventListener(this);

        prefixes = Set.of(
                String.format("<@!%s>", secretProvider.getDiscordClientId()),
                String.format("<@%s>", secretProvider.getDiscordClientId()),
                "%"
        );

        testingGuildId = secretProvider.getTestingGuildId();
        producitonCommands = List.of(environment.getActiveProfiles()).contains("production-commands");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getGuild().getId().equals(testingGuildId) == producitonCommands) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw().trim();

        String prefix = prefixes.stream()
                .filter(message::startsWith)
                .findAny()
                .orElse(null);

        if (prefix == null) {
            return;
        }

        message = message.substring(prefix.length()).trim();

        switch (message) {
            case "doggo":
                doggo(event);
                break;
            case "export":
                export(event);
                break;
            default:
                event.getChannel().sendMessage("🤷 Unknown command").queue();
        }
    }

    private void doggo(MessageReceivedEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(new Color(155, 89, 182))
                .setImage(doggoUriProvider.randomUri().toString())
                .build();

        event.getChannel().sendMessage(embed).queue();
    }

    private void export(MessageReceivedEvent event) {
        try {
            roleManager.authenticate(event.getAuthor().getId());
        } catch (NotStaffException e) {
            event.getChannel().sendMessage("👮 Not permitted").queue();
            return;
        }

        Message statusMessage = event.getChannel()
                .sendMessage(createStatusMessageContent(0))
                .complete();

        long startTime = System.currentTimeMillis();
        AtomicLong lastUpdateTime = new AtomicLong(startTime);

        ChannelExporter.Result exportResult = channelExporter.createExport(event.getChannel(), count -> {
            if (System.currentTimeMillis() - lastUpdateTime.get() > 2000) {
                lastUpdateTime.set(System.currentTimeMillis());
                statusMessage.editMessage(createStatusMessageContent(count)).queue();
            }
        });

        statusMessage
                .editMessage(createStatusMessageContent(exportResult.getMessageCount()))
                .queue();

        event.getChannel().sendMessage(String.format(
                "👍️ Exported `%s` messages in `%.1f` seconds",
                exportResult.getMessageCount(),
                (System.currentTimeMillis() - startTime) / 1000f
        )).queue();

        event.getAuthor().openPrivateChannel().queue(dm -> {
            dm.sendFile(exportResult.getContent(), "channel-export.json.gzip").queue(message -> {
                event.getChannel().sendMessage("📨 Sent via DM").queue();
            }, error -> {
                event.getChannel().sendMessage("❌ Could not send via DM").queue();
            });
        }, error -> {
            event.getChannel().sendMessage("❌ Could not open DM").queue();
        });
    }

    private String createStatusMessageContent(int messagesProcessed) {
        return String.format("⚙️ Exporting channel, `%s` messages exported thus far", messagesProcessed);
    }
}

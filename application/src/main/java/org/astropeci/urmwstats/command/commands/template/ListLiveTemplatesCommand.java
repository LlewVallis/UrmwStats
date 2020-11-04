package org.astropeci.urmwstats.command.commands.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.template.LiveTemplateRepository;
import org.astropeci.urmwstats.template.LiveTemplateTracker;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ListLiveTemplatesCommand implements Command {

    private final LiveTemplateRepository liveTemplateRepository;

    @Override
    public String label() {
        return "list-live-templates";
    }

    @Override
    public String usage() {
        return "list-live-templates";
    }

    @Override
    public String helpDescription() {
        return "List all live templates which are being updated every minute";
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

        Set<LiveTemplateTracker> trackers = liveTemplateRepository.all();
        if (trackers.size() == 0) {
            event.getChannel().sendMessage("🍃 There are no active live templates").queue();
        } else {
            StringBuilder builder = new StringBuilder("🍃 Live templates:");

            trackers.forEach(tracker -> {
                builder.append(String.format(
                        "\n        • https://discord.com/channels/%s/%s/%s",
                        tracker.getGuildId(), tracker.getChannelId(), tracker.getMessageId()
                ));
            });

            event.getChannel().sendMessage(builder).queue();
        }
    }
}

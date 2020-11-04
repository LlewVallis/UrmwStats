package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.TimeUtil;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.metrics.Metrics;
import org.astropeci.urmwstats.metrics.MetricsStore;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricsCommand implements Command {

    private final MetricsStore metricsStore;

    @Override
    public String label() {
        return "metrics";
    }

    @Override
    public String usage() {
        return "metrics";
    }

    @Override
    public String helpDescription() {
        return "Shows statistics about the URMW Stats service";
    }

    @Override
    public int helpPriority() {
        return 1;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        Metrics metrics = metricsStore.getMetrics();

        Duration jvmUptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        String durationString = TimeUtil.durationString(jvmUptime);
        if (durationString.isEmpty()) {
            durationString = "< 1 minute";
        }

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle("📊 Metrics")
                .addField("Commands run", Integer.toString(metrics.getCommandsRun()), true)
                .addField("Doggos provided", Integer.toString(metrics.getDoggosProvided()), true)
                .addField("Uptime", durationString, true);

        event.getChannel().sendMessage(embed.build()).queue();
    }
}

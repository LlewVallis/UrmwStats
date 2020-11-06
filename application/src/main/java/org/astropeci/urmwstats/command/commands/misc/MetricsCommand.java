package org.astropeci.urmwstats.command.commands.misc;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.metrics.Metrics;
import org.astropeci.urmwstats.metrics.MetricsStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricsCommand implements Command {

    private final JDA jda;
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
    public HelpSection section() {
        return HelpSection.MISC;
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

        EmbedBuilder embed = CommandUtil.coloredEmbedBuilder()
                .setTitle("📊 Metrics")
                .addField("Commands run", Integer.toString(metrics.getCommandsRun()), true)
                .addField("Doggos provided", Integer.toString(metrics.getDoggosProvided()), true)
                .addField("Discord ping", jda.getGatewayPing() + "ms", true);

        event.getChannel().sendMessage(embed.build()).queue();
    }
}

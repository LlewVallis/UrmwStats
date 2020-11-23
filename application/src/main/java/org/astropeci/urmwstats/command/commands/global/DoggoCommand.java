package org.astropeci.urmwstats.command.commands.global;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.DoggoProvider;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.metrics.MetricsStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DoggoCommand implements Command {

    private final DoggoProvider doggoProvider;
    private final MetricsStore metricsStore;

    @Override
    public String label() {
        return "doggo";
    }

    @Override
    public String usage() {
        return "doggo";
    }

    @Override
    public String helpDescription() {
        return "Presents you with a 🌶️ doggo";
    }

    @Override
    public HelpSection section() {
        return HelpSection.GLOBAL;
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

        event.getChannel().sendFile(doggoProvider.randomDoggo(), "doggo.jpg").complete();
        metricsStore.doggoProvided();
    }
}

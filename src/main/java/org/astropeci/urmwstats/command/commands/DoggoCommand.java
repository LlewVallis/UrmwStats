package org.astropeci.urmwstats.command.commands;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.DoggoUriProvider;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DoggoCommand implements Command {

    private final DoggoUriProvider doggoUriProvider;

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
    public int helpPriority() {
        return 3;
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

        MessageEmbed embed = CommandUtil.coloredEmbedBuilder()
                .setImage(doggoUriProvider.randomUri().toString())
                .build();

        event.getChannel().sendMessage(embed).queue();
    }
}

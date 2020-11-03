package org.astropeci.urmwstats.command;

import lombok.experimental.UtilityClass;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.PlayerRepository;

import java.awt.*;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class CommandUtil {

    private static final Pattern TEMPLATE_NAME_PATTERN = Pattern.compile("[-_A-Za-z0-9]*[A-Za-z][-_A-Za-z0-9]*");

    public void throwWrongNumberOfArguments() {
        throw new CommandException("❌ Wrong number of arguments");
    }

    public EmbedBuilder coloredEmbedBuilder() {
        return new EmbedBuilder().setColor(new Color(155, 89, 182));
    }

    public Player matchPlayer(PlayerRepository playerRepository, String fuzzyName) {
        if (playerRepository.size() == 0) {
            return null;
        }

        Map<String, Player> playersByName = playerRepository.byRanking().stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));

        BoundExtractedResult<String> searchResult = FuzzySearch.extractOne(
                fuzzyName.toLowerCase(),
                playersByName.keySet(),
                (ToStringFunction<String>) String::toLowerCase
        );

        if (searchResult.getScore() < 75) {
            return null;
        }

        String name = searchResult.getReferent();
        return playersByName.get(name);
    }

    public String templateName(String input) {
        if (!TEMPLATE_NAME_PATTERN.matcher(input).matches()) {
            throw new CommandException("❌ Invalid template name");
        }

        return input;
    }

    public String ordinalSuffix(int ordinal) {
        if (Math.abs(ordinal) > 10 && Math.abs(ordinal) < 14) {
            return "th";
        }

        if (Integer.toString(ordinal).endsWith("1")) {
            return "st";
        } else if (Integer.toString(ordinal).endsWith("2")) {
            return "nd";
        } else if (Integer.toString(ordinal).endsWith("3")) {
            return "rd";
        } else {
            return "th";
        }
    }

    public static MessageChannel parseChannel(String input, MessageReceivedEvent event, JDA jda) {
        if (!input.startsWith("<#") || !input.endsWith(">")) {
            return throwInvalidChannelSyntax(event);
        }

        input = input.substring(2, input.length() - 1);

        MessageChannel channel;
        try {
            channel = jda.getTextChannelById(input);
        } catch (NumberFormatException e) {
            return throwInvalidChannelSyntax(event);
        }

        if (channel == null) {
            throw new CommandException("❌ That channel either doesn't exist or URMW Stats doesn't have permission to see it");
        }

        return channel;
    }

    private <T> T throwInvalidChannelSyntax(MessageReceivedEvent event) {
        throw new CommandException(String.format(
                "❌ Invalid channel syntax, tag it directly (e.g. <#%s>)",
                event.getChannel().getId()
        ));
    }
}

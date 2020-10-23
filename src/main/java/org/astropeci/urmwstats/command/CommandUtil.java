package org.astropeci.urmwstats.command;

import lombok.experimental.UtilityClass;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.EmbedBuilder;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.PlayerRepository;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CommandUtil {

    public void throwWrongNumberOfArguments() {
        throw new CommandException("❌ Wrong number of arguments");
    }

    public EmbedBuilder coloredEmbedBuilder() {
        return new EmbedBuilder().setColor(new Color(155, 89, 182));
    }

    public Optional<Player> matchPlayer(PlayerRepository playerRepository, String fuzzyName) {
        if (playerRepository.size() == 0) {
            return Optional.empty();
        }

        Map<String, Player> playersByName = playerRepository.getPlayersByRanking().stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));

        BoundExtractedResult<String> searchResult = FuzzySearch.extractOne(
                fuzzyName.toLowerCase(),
                playersByName.keySet(),
                (ToStringFunction<String>) String::toLowerCase
        );

        if (searchResult.getScore() < 75) {
            return Optional.empty();
        }

        String name = searchResult.getReferent();
        return Optional.of(playersByName.get(name));
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
}

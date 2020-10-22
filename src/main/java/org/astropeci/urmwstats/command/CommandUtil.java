package org.astropeci.urmwstats.command;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

@UtilityClass
public class CommandUtil {

    public <T> T throwWrongNumberOfArguments() {
        throw new CommandException("❌ Wrong number of arguments");
    }

    public EmbedBuilder coloredEmbedBuilder() {
        return new EmbedBuilder().setColor(new Color(155, 89, 182));
    }
}

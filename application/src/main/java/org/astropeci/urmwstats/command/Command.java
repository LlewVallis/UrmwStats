package org.astropeci.urmwstats.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public interface Command {

    String label();

    String usage();

    String helpDescription();

    HelpSection section();

    boolean isStaffOnly();

    void execute(List<String> arguments, MessageReceivedEvent event);
}

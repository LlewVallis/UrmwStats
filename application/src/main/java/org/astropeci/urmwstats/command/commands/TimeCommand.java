package org.astropeci.urmwstats.command.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.TimeUtil;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class TimeCommand implements Command {

    @Override
    public String label() {
        return "time";
    }

    @Override
    public String usage() {
        return "time [time...]";
    }

    @Override
    public String helpDescription() {
        return "Show information about the current time or some other time";
    }

    @Override
    public int helpPriority() {
        return 4;
    }

    @Override
    public boolean isStaffOnly() {
        return false;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Instant time;
        if (arguments.size() == 0) {
            time = now;
        } else {
            String input = String.join(" ", arguments);
            time = TimeUtil.parseDate(input, now);
        }

        if (time == null) {
            throw new CommandException("❌ Could not infer time");
        }

        Duration duration = Duration.between(now, time).abs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'UTC' MMM d");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (duration.toMinutes() < 1) {
            event.getChannel().sendMessageFormat(
                    "🕑 Current time is %s",
                    dateFormat.format(Date.from(time))
            ).queue();
        } else {
            event.getChannel().sendMessageFormat(
                    "🕑 %s %s %s%s",
                    dateFormat.format(Date.from(time)),
                    now.isBefore(time) ? "is in" : "was",
                    TimeUtil.durationString(duration),
                    now.isBefore(time) ? "" : " ago"
            ).queue();
        }
    }
}

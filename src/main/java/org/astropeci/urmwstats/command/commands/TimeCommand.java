package org.astropeci.urmwstats.command.commands;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
        Instant now = Instant.now();

        Instant time;
        if (arguments.size() == 0) {
            time = now;
        } else {
            String input = String.join(" ", arguments);
            time = inferTime(input, now);
        }

        if (time == null) {
            throw new CommandException("❌ Could not infer time");
        }

        Duration duration = Duration.between(now, time).abs();
        if (duration.toSecondsPart() >= 30) {
            duration = duration.plusMinutes(1);
        }

        duration.truncatedTo(ChronoUnit.MINUTES);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'UTC' MMM d");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (duration.toMinutes() < 1) {
            event.getChannel().sendMessageFormat(
                    "🕑 Current time is %s",
                    dateFormat.format(Date.from(time))
            ).queue();
        } else {
            List<String> durationSegments = new ArrayList<>();

            if (duration.toDaysPart() > 0) {
                durationSegments.add(duration.toDaysPart() + " day(s)");
            }

            if (duration.toHoursPart() > 0) {
                durationSegments.add(duration.toHoursPart() + " hour(s)");
            }

            if (duration.toMinutesPart() > 0) {
                durationSegments.add(duration.toMinutesPart() + " minute(s)");
            }

            StringBuilder durationString = new StringBuilder();
            for (int i = 0; i < durationSegments.size(); i++) {
                String prefix = i == 0 ? "" : i == durationSegments.size() - 1 ? " and " : ", ";
                durationString.append(prefix).append(durationSegments.get(i));
            }

            event.getChannel().sendMessageFormat(
                    "🕑 %s %s %s%s",
                    dateFormat.format(Date.from(time)),
                    now.isBefore(time) ? "is in" : "was",
                    durationString,
                    now.isBefore(time) ? "" : " ago"
            ).queue();
        }
    }

    private Instant inferTime(String input, Instant now) {
        Parser parser = new Parser(TimeZone.getTimeZone("GMT"));
        List<DateGroup> groups = parser.parse(input, Date.from(now));

        if (groups.size() != 1) {
            return null;
        }

        DateGroup group = groups.get(0);

        if (group.getDates().size() != 1) {
            return null;
        }

        if (group.isRecurring()) {
            return null;
        }

        Date result = group.getDates().get(0);
        return result.toInstant();
    }
}

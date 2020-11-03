package org.astropeci.urmwstats;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@UtilityClass
public class TimeUtil {

    public Instant parseDate(String input, Instant now) {
        boolean allowRelative = true;
        if (now == null) {
            now = Instant.now();
            allowRelative = false;
        }

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

        if ((group.isTimeInferred() || group.isDateInferred()) && !allowRelative) {
            return null;
        }

        Date dateResult = group.getDates().get(0);
        return dateResult.toInstant();
    }

    public String durationString(Duration duration) {
        duration = duration.abs();

        List<String> durationSegments = new ArrayList<>();

        if (duration.toDaysPart() > 0) {
            durationSegments.add(String.format(
                    "%s day%s",
                    duration.toDaysPart(),
                    duration.toDaysPart() > 1 ? "s" : ""
            ));
        }

        if (duration.toHoursPart() > 0) {
            durationSegments.add(String.format(
                    "%s hour%s",
                    duration.toHoursPart(),
                    duration.toHoursPart() > 1 ? "s" : ""
            ));
        }

        if (duration.toMinutesPart() > 0) {
            durationSegments.add(String.format(
                    "%s minute%s",
                    duration.toMinutesPart(),
                    duration.toMinutesPart() > 1 ? "s" : ""
            ));
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < durationSegments.size(); i++) {
            String prefix = i == 0 ? "" : i == durationSegments.size() - 1 ? " and " : ", ";
            result.append(prefix).append(durationSegments.get(i));
        }

        return result.toString();
    }
}

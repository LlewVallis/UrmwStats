package org.astropeci.urmwstats;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@UtilityClass
public class TimeUtils {

    public Instant parseDate(String input) {
        return parseDate(input, Instant.now());
    }

    public Instant parseDate(String input, Instant now) {
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

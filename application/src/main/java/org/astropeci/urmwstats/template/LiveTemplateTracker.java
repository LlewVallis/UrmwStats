package org.astropeci.urmwstats.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveTemplateTracker {

    private String guildId;
    private String channelId;
    private String messageId;

    private String templateName;
    private List<String> variables;
    private Instant baselineTime;
}

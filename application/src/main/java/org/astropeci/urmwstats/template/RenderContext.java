package org.astropeci.urmwstats.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class RenderContext {

    private final List<String> variables;

    private final Instant baselineTime;
    private final Instant now;

    private boolean timeDependent;
    private boolean usingCountdowns;

    public void setTimeDependent() {
        timeDependent = true;
    }

    public void setUsingCountdowns() {
        usingCountdowns = true;
    }
}

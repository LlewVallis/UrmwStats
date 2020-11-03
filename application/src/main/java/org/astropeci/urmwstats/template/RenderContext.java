package org.astropeci.urmwstats.template;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class RenderContext {

    List<String> variables;
    Instant now;
}

package org.astropeci.urmwstats.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tourney {

    @EqualsAndHashCode.Include
    private int id;
    private Set<String> first;
    private Set<String> second;
    private Set<String> third;
    private Instant timestamp;
}

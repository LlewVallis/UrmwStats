package org.astropeci.urmwstats.data;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;

@With
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MatchParticipant {

    @EqualsAndHashCode.Include
    String name;
    Skill skillBefore;
    Skill skillAfter;
}

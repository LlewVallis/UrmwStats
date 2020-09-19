package org.astropeci.urmwstats.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match {

    @EqualsAndHashCode.Include
    private int id;
    private Set<MatchParticipant> winners;
    private Set<MatchParticipant> losers;
    private Instant timestamp;

    @JsonIgnore
    public Set<MatchParticipant> getParticipants() {
        Set<MatchParticipant> result = new HashSet<>();
        result.addAll(winners);
        result.addAll(losers);
        return result;
    }
}

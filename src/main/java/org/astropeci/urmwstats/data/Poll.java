package org.astropeci.urmwstats.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Poll {

    @BsonId
    @EqualsAndHashCode.Include
    private ObjectId id = ObjectId.get();

    private String name;
    private List<String> options;
    private Set<Voter> voters;

    public Poll(String name, List<String> options) {
        this.name = name;
        this.options = options;
        voters = new HashSet<>();
    }

    @BsonIgnore
    public Set<String> getWinningOptions() {
        Set<String> optionSet = new HashSet<>(this.options);

        while (true) {
            Map<String, Integer> votesByOption = new HashMap<>(
                    optionSet.stream()
                            .collect(Collectors.toMap(Function.identity(), key -> 0))
            );

            for (Voter voter : voters) {
                for (int preferenceIndex : voter.getPreferences()) {
                    String preference = options.get(preferenceIndex);

                    if (optionSet.contains(preference)) {
                        votesByOption.put(preference, votesByOption.get(preference) + 1);
                        break;
                    }
                }
            }

            int maximumVotes = votesByOption.values().stream().max(Integer::compareTo).orElseThrow();
            int minimumVotes = votesByOption.values().stream().min(Integer::compareTo).orElseThrow();

            if (maximumVotes == minimumVotes) {
                return optionSet;
            } else {
                votesByOption.forEach((option, votes) -> {
                    if (votes == minimumVotes) {
                        optionSet.remove(option);
                    }
                });
            }
        }
    }
}

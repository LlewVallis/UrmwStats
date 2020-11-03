package org.astropeci.urmwstats.poll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Voter {

    @BsonProperty("id")
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private List<Integer> preferences;
}

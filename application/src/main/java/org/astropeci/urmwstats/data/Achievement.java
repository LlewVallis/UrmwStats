package org.astropeci.urmwstats.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Achievement {

    @EqualsAndHashCode.Include
    private String name;
    private String description;
    private List<String> playersCompleted;
}

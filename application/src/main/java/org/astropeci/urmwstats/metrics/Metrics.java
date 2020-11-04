package org.astropeci.urmwstats.metrics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Metrics implements Cloneable {

    private int commandsRun;
    private int doggosProvided;

    public Metrics(Metrics other) {
        commandsRun = other.getCommandsRun();
        doggosProvided = other.getDoggosProvided();
    }
}

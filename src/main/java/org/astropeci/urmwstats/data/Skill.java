package org.astropeci.urmwstats.data;

import lombok.Value;
import lombok.With;

@With
@Value
public class Skill {

    double mean;
    double deviation;

    public double getTrueskill() {
        return mean - 3 * deviation;
    }

    public static Skill fromTrueskill(double trueskill, double deviation) {
        return new Skill(trueskill + 3 * deviation, deviation);
    }
}

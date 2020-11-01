package org.astropeci.urmwstats.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {

    @EqualsAndHashCode.Include
    private String name;
    private Skill skill;
    private Skill peakSkill;

    private int ranking;

    private int wins;
    private int losses;

    private double fractionalTourneyWins;
    private int timesPlacedFirst;
    private int timesPlacedSecond;
    private int timesPlacedThird;

    private Map<String, Integer> winsAgainst;
    private Map<String, Integer> lossesAgainst;

    private int streak;

    public String getRankName() {
        double trueskill = skill.getTrueskill();
        double deviation = skill.getDeviation();

        if (deviation > 75) {
            return "unranked";
        } else if (trueskill >= 1600) {
            if (ranking == 0) {
                return "grand champion";
            } else {
                return "champion";
            }
        } else if (trueskill >= 1510) {
            return "diamond";
        } else if (trueskill >= 1430) {
            return "platinum";
        } else if (trueskill >= 1350) {
            return "gold";
        } else if (trueskill >= 1260) {
            return "silver";
        } else {
            return "bronze";
        }
    }
}

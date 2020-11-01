package org.astropeci.urmwstats;

import lombok.Value;

@Value
public class TrueskillSettings {
    double mu = 1475;
    double sigma = 100;
    double beta = 50;
    double tau = 5;
    double drawProbability = 0.05;
}

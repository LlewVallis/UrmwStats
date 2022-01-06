package org.astropeci.urmwstats;

import lombok.Value;

@Value
public class TrueskillSettings {
    double mu = 1500;
    double sigma = 100;
    double beta = 65;
    double tau = 0.5;
    double drawProbability = 0.05;
}

import React from "react";
import { Pie } from "react-chartjs-2";
import { interpolateWarm } from "d3-scale-chromatic";
import { StandardDataContext } from "../App";
import { StandardData } from "../../api/standard-data";

const TourneyWinRates = () => (
  <StandardDataContext.Consumer>
    { data => {
      const { values, colors, labels } = createChartData(data);

      return (
        <Pie
          data={{
            datasets: [{
              data: values,
              backgroundColor: colors,
            }],
            labels: labels,
          }}
          options={{
            legend: {
              position: "bottom",
            },
          }}
        />
      );
    }}
  </StandardDataContext.Consumer>
);

interface ChartData {
  values: number[];
  labels: string[];
  colors: string[];
}

function createChartData({ info, players }: StandardData): ChartData {
  const values: number[] = [];
  const labels: string[] = [];
  const colors: string[] = [];

  const winList: [string, number][] = players
    .filter(player => player.fractionalTourneyWins > 0)
    .map(player => [player.name, player.fractionalTourneyWins]);

  winList.sort((a, b) => b[1] - a[1]);

  let winsProcessed = 0;
  for (const [name, value] of winList) {
    values.push(value);
    labels.push(name);
    colors.push(interpolateWarm((winsProcessed / info.tourneyCount) * 0.75))
    winsProcessed += value;
  }

  return { values, labels, colors };
}

export default TourneyWinRates;
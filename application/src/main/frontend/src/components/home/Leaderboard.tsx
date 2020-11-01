import React from "react";
import { Bar } from "react-chartjs-2";
import ChartErrorBars from "chartjs-plugin-error-bars";
import { interpolateWarm } from "d3-scale-chromatic";
import { StandardDataContext } from "../App";
import { StandardData } from "../../api/standard-data";

const Leaderboard = () => (
  <StandardDataContext.Consumer>
    { data => {
      const { trueskills, errors, colors, labels, minPoint, maxPoint } = createChartData(data);

      return <Bar 
        data={{
          datasets: [
            {
              data: trueskills,
              borderColor: "#666",
              errorBars: errors,
              backgroundColor: colors,
              label: "Current Trueskill",
            },
          ],
          labels: labels,
        }}
        options={{
          legend: {
            display: false,
          },
          animation: {
            duration: 0,
          },
          scales: {
            yAxes: [{
              scaleLabel: {
                display: true,
                labelString: "Trueskill",
              },
              ticks: {
                suggestedMin: minPoint,
                suggestedMax: maxPoint,
              },
            }],
            xAxes: [{
              ticks: {
                autoSkip: false,
              }
            }],
          },
        }}
        plugins={[ChartErrorBars]}
      />
    }}
  </StandardDataContext.Consumer>
);

interface ChartData {
  trueskills: number[];
  errors: Record<string, { plus: number; minus: number }>;
  labels: string[];
  colors: string[];
  minPoint: number;
  maxPoint: number;
}

function createChartData({ players }: StandardData): ChartData {
  const trueskills: number[] = [];
  const errors: Record<string, { plus: number; minus: number }> = {};
  const labels: string[] = [];
  const colors: string[] = [];

  let minPoint = Infinity;
  let maxPoint = 0;

  const rankedPlayers = players.filter(player => player.rankName !== "unranked");

  for (let i = 0; i < rankedPlayers.length; i++) {
    const player = rankedPlayers[i]
    const trueskill = player.skill.trueskill;
    const rd = player.skill.deviation;

    trueskills.push(trueskill)
    labels.push(player.name)

    errors[player.name] = { plus: rd, minus: -rd }

    colors.push(interpolateWarm((i + 1) / rankedPlayers.length * 0.6 * 0.6 + 0.4))

    if (minPoint > trueskill - rd) {
      minPoint = trueskill - rd
    }

    if (maxPoint < trueskill + rd) {
      maxPoint = trueskill + rd
    }
  }

  return { trueskills, errors, labels, colors, minPoint, maxPoint };
}

export default Leaderboard;
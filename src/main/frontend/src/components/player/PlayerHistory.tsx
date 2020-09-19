import React from "react";
import { Line } from "react-chartjs-2";

import { PrimaryColor, SecondaryColor } from "../App";
import { Player } from "../../api/player";
import { Match } from "../../api/match";

const PlayerHistory = ({ player, history }: { player: Player, history: Match[] }) => {
  const { trueskillDataset, deviationDataset, minTrueskill, maxTrueskill } = createDatasets(player, history);

  return (
    <Line
      data={{
        datasets: [trueskillDataset, deviationDataset],
      }}
      options={{
        animation: {
          duration: 0,
        },
        legend: {
          position: "bottom",
        },
        scales: {
          xAxes: [{
            type: "time",
          }],
          yAxes: [{
            id: "trueskill",
            ticks: {
              suggestedMin: minTrueskill,
              suggestedmax: maxTrueskill,
            },
            scaleLabel: {
              display: true,
              labelString: "Trueskill",
            },
          }, {
            id: "deviation",
            position: "right",
            ticks: {
              min: 0,
              max: 100,
            },
            scaleLabel: {
              display: true,
              labelString: "Rating Deviation",
            },
          }]
        },
      }}
    />
  );
}

function createDatasets(player: Player, history: Match[]): {
  trueskillDataset: any;
  deviationDataset: any,
  minTrueskill: number;
  maxTrueskill: number;
} {
  const matches = [...history];
  matches.reverse();

  const datapoints = [];

  for (const match of matches) {
    for (const participant of [...match.winners, ...match.losers]) {
      if (participant.name === player.name) {
        datapoints.push({
          time: new Date(match.timestamp),
          trueskill: participant.skillAfter.trueskill,
          deviation: participant.skillAfter.deviation,
        });
      }
    }
  }

  datapoints.push({
    time: new Date(),
    trueskill: player.skill.trueskill,
    deviation: player.skill.deviation,
  });

  let timePerCluster = 0;
  if (datapoints.length > 0) {
    const totalTime = datapoints[datapoints.length - 1].time.getTime() - datapoints[0].time.getTime();
    timePerCluster = totalTime / 50;
  }

  datapoints.reverse();

  for (let i = 1; i < datapoints.length; i++) {
    const nextTime = datapoints[i - 1].time;
    const currentTime = datapoints[i].time;

    if (nextTime.getTime() - currentTime.getTime() < timePerCluster) {
      datapoints.splice(i, 1);
      i--;
    }
  }

  datapoints.reverse();

  const trueskillData = [];
  const deviationData = [];
  let minTrueskill = Infinity;
  let maxTrueskill = 0;

  for (const { time, trueskill, deviation } of datapoints) {
    minTrueskill = Math.min(trueskill, minTrueskill);
    maxTrueskill = Math.max(trueskill, maxTrueskill);

    trueskillData.push({
      x: time,
      y: trueskill,
    });

    deviationData.push({
      x: time,
      y: deviation,
    });
  }

  return {
    trueskillDataset: {
      data: trueskillData,
      showLine: true,
      fill: false,
      yAxisID: "trueskill",
      label: "Trueskill",
      borderColor: PrimaryColor,
      lineTension: 0.15,
      cubicInterpolationMode: "monotonic",
    },
    deviationDataset: {
      data: deviationData,
      showLine: true,
      fill: false,
      yAxisID: "deviation",
      label: "Rating Deviation",
      borderColor: SecondaryColor,
      lineTension: 0.15,
      cubicInterpolationMode: "monotonic",
    },
    minTrueskill, maxTrueskill
  };
}

export default PlayerHistory;
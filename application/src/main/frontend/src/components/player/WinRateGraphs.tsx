import React, { ReactNode } from "react";
import { interpolateWarm } from "d3-scale-chromatic";
import { Pie, Bar } from "react-chartjs-2";
import { Player } from "../../api/player";
import { PrimaryColor, SecondaryColor } from "../App";

const WinRateGraphs = ({ player }: { player: Player }) => {
  if (player.wins + player.losses === 0) {
    return null;
  }

  return (
    <div style={{
      display: "flex",
      flexWrap: "wrap",
      justifyContent: "center",
      maxWidth: "70rem",
      margin: "0 auto",
    }}>
      {player.losses > 0 ? (
        <FlexElement>
          <h1>Top opponents</h1>
          <TopOpponents player={player}/>
        </FlexElement>
      ) : null}
      <FlexElement>
        <h1>Win rate</h1>
        <WinRate player={player} />
      </FlexElement>
    </div>
  );
};

const FlexElement = ({ children }: { children: ReactNode }) => (
  <div style={{
    marginTop: "6rem",
    width: "35rem",
  }}>
    {children}
  </div>
);

const WinRate = ({ player }: { player: Player }) => (
  <div style={{
    width: "35rem",
  }}>
    <Pie 
      data={{
        datasets: [{
          data: [player.wins, player.losses],
          backgroundColor: [PrimaryColor, SecondaryColor],
        }],
        labels: ["Wins", "Losses"],
      }}
      options={{
        legend: {
          position: "bottom",
        },
      }}
    />
  </div>
);

const TopOpponents = ({ player }: { player: Player }) => {
  let opponents = Object.entries(player.lossesAgainst);

  opponents.sort(([nameA, lossesA], [nameB, lossesB]) => {
    let result = lossesB - lossesA;
    
    if (result === 0) {
      const winsA = player.winsAgainst[nameA] || 0;
      const winsB = player.winsAgainst[nameB] || 0;

      result = winsA - winsB;
    }

    return result;
  })

  opponents = opponents.slice(0, 4);

  const values = []
  const colors = []
  const labels = []

  for (let i = 0; i < opponents.length; i++) {
    const [name, losses] = opponents[i]

    labels.push(name)
    values.push(losses)
    colors.push(interpolateWarm((i + 1) / opponents.length * 0.75))
  }

  return (
    <div style={{
      width: "35rem",
    }}>
      <Bar 
        data={{
          datasets: [{
            data: values,
            backgroundColor: colors,
            barPercentage: 0.9 * (values.length / 4),
          }],
          labels: labels,
        }}
        options={{
          legend: {
            display: false,
          },
          scales: {
            yAxes: [{
              scaleLabel: {
                display: true,
                labelString: "Losses against",
              },
              ticks: {
                beginAtZero: true,
                precision: 0,
              },
            }],
          },
        }}
      />
    </div>
  );
};

export default WinRateGraphs;
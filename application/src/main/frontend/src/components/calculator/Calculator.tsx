import React, { useState } from "react";
import Select, { OptionsType } from "react-select";
import { Rating, TrueSkill } from "ts-trueskill";
import { Match, MatchParticipant } from "../../api/match";
import { Player } from "../../api/player";
import Skill from "../../api/skill";
import { StandardData } from "../../api/standard-data";
import { PrimaryColor, SecondaryColor, StandardDataContext } from "../App";
import { MatchCard } from "../history/History";
import { ArrowSwitchIcon } from "@primer/octicons-react";
import { Pie } from "react-chartjs-2";

const Calculator = () => {
  const [winnerValues, setWinnerValues] = useState<SelectorValues>([]);
  const [loserValues, setLoserValues] = useState<SelectorValues>([]);

  const winners = winnerValues.map(value => value.value);
  const losers = loserValues.map(value => value.value);

  return (
    <StandardDataContext.Consumer>
      { data => {
        const match = (winners.length > 0 && losers.length > 0) ? createMatch(data, winners, losers) : null;

        return (
          <div style={{
            textAlign: "center",
          }}>
            <h1>Match calculator</h1>

            <div style={{
              width: "50rem",
              margin: "0 auto",
              marginTop: "3rem",
            }}>
              <div style={{
                display: "flex",
                justifyContent: "center",
              }}>
                <Selector data={data} values={winnerValues} label="Winners" onChange={values => setWinnerValues(values)} />

                <span
                  className="grow-on-hover"
                  onClick={() => {
                    setWinnerValues(loserValues);
                    setLoserValues(winnerValues);
                  }}
                  style={{
                    alignSelf: "center",
                    margin: "0 2rem",
                    transition: "transform .25s ease-out",
                    cursor: "pointer",
                  }}
                >
                  <ArrowSwitchIcon size={32} />
                </span>

                <Selector data={data} values={loserValues} label="Losers" onChange={values => setLoserValues(values)} />
              </div>

              {match ? (
                <div style={{
                  marginTop: "4rem",
                }}>
                  <MatchCard match={match} />
                </div>
              ) : null}

              {shouldShowHistory(winners, losers) ? (
                <div style={{
                  marginTop: "4rem",
                }}>
                  <HistoryCard winner={winners[0]} loser={losers[0]} />
                </div>
              ) : null}
            </div>
          </div>
        );
      }}
    </StandardDataContext.Consumer>
  );
};

function shouldShowHistory(winners: Player[], losers: Player[]): boolean {
  if (winners.length !== 1) return false;
  if (losers.length !== 1) return false;

  const [ winner ] = winners;
  const [ loser ] = losers;

  if (winner.winsAgainst[loser.name] === undefined && winner.lossesAgainst[loser.name] === undefined) return false;

  return true;
}

const HistoryCard = ({ winner, loser }: { winner: Player, loser: Player }) => (
  <div>
    <div style={{
      marginBottom: "1rem",
    }}>
      <h2>Match history</h2>
    </div>

    <Pie
      data={{
        datasets: [{
          data: [winner.winsAgainst[loser.name], winner.lossesAgainst[loser.name]],
          backgroundColor: [PrimaryColor, SecondaryColor],
        }],
        labels: [`${winner.name} wins`, `${loser.name} wins`],
      }}
      options={{
        legend: {
          position: "bottom",
        },
      }}
    />

    <i>Match history only available for 1v1s</i>
  </div>
);

type SelectorValues = OptionsType<{ label: string, value: Player }>;

interface SelectorProps {
  data: StandardData;
  values: SelectorValues;
  label: string;
  onChange: (value: SelectorValues) => void;
}

const Selector = ({ data, values, label, onChange }: SelectorProps) => (
  <div style={{
    width: "100%",
    flexGrow: 1,
  }}>
    <b style={{
      margin: "0.5rem",
      fontSize: "1.1rem",
    }}>
      {label}
    </b>
    <div style={{
      textAlign: "left",
    }}>
      <Select 
        isMulti
        placeholder="Players..."
        options={data.players.map(player => ({ value: player, label: player.name }))}
        value={values}
        theme={theme => ({
          ...theme,
          colors: {
            ...theme.colors,
            primary: PrimaryColor,
            primary25: "#eee",
            danger: "black",
            dangerLight: "#bbb",
          },
        })}
        onChange={values => {
          values = values || [];
          onChange(values as SelectorValues);
        }}
      />
    </div>
  </div>
);

function createMatch(data: StandardData, winners: Player[], losers: Player[]): Match | null {
  try {
    const settings = data.info.trueskillSettings;

    const trueskill = new TrueSkill(settings.mu, undefined, undefined, undefined, settings.drawProbability);
    trueskill.sigma = settings.sigma;
    trueskill.beta = settings.beta;
    trueskill.tau = settings.tau;

    const createTeamRatings = (team: Player[]) => team.map(player => new Rating(player.skill.mean, player.skill.deviation));
    const oldWinnerRatings = createTeamRatings(winners);
    const oldLoserRatings = createTeamRatings(losers);
    const [newWinnerRatings, newLoserRatings] = trueskill.rate([oldWinnerRatings, oldLoserRatings], [0, 1]);

    return {
      id: 0,
      winners: createParticipants(winners, newWinnerRatings),
      losers: createParticipants(losers, newLoserRatings),
      timestamp: new Date().toISOString(),
    };
  } catch {
    return null;
  }
}

function createParticipants(players: Player[], newRatings: Rating[]): MatchParticipant[] {
  return players.map((player, i) => {
    const newRating = newRatings[i];

    const newTrueskill = Math.ceil(newRating.mu - 3 * newRating.sigma);
    const newDeviation = Math.ceil(newRating.sigma);

    const newSkill: Skill = {
      mean: newTrueskill + 3 * newDeviation,
      trueskill: newTrueskill,
      deviation: newDeviation,
    };

    return {
      name: player.name,
      skillBefore: player.skill,
      skillAfter: newSkill,
    };
  });
}

export default Calculator;
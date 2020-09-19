import React, { ReactNode, useState } from "react";
import { Link } from "react-router-dom";
import { InView } from "react-intersection-observer";
import { ArrowRightIcon } from "@primer/octicons-react";
import { Button } from "react-bootstrap";
import MatchHistoryProvider from "../MatchHistoryProvider";
import { Match, MatchParticipant } from "../../api/match";
import PageSpinner from "../PageSpinner";
import { StandardDataContext } from "../App";
import PlayerSearch from "../PlayerSearch";

const History = () => {
  const [ count, setCount ] = useState(20);
  const [ player, setPlayer ] = useState<string | undefined>(undefined);
  
  return (
    <>
      <MatchHistoryProvider count={count} player={player}>
        { history => (
          <>
            <div style={{
              display: "flex",
            }}>
              <h1>Match log</h1>

              <div style={{
                textAlign: "right",
                flexGrow: 1,
              }}>
                {player ? (
                  <Button 
                    size="lg" 
                    variant="outline-light"
                    onClick={() => setPlayer(undefined)}
                  >
                    Clear filter
                  </Button>
                ) : (
                  <PlayerSearch placeholder="Filter by player" callback={name => setPlayer(name)} />
                )}
              </div>
            </div>

            <div style={{
              display: "grid",
              gridTemplateColumns: "repeat(2, 1fr)",
              gap: "1rem",
            }}>
                { history?.map(match => <MatchCard key={match.id} match={match} />) }
            </div>

            <StandardDataContext.Consumer>
              { data => {
                let matchCap;
                if (player) {
                  const playerData = data.players.find(playerData => playerData.name === player)!;
                  matchCap = playerData.wins + playerData.losses;
                } else {
                  matchCap = data.info.matchCount;
                }

                if (history !== null && history.length === matchCap) {
                  return null;
                }

                return (
                  <InView onChange={(inView) => {
                    if (inView && history) {
                      setCount(count + 50);
                    }
                  }}>
                    <PageSpinner errored={false} />
                  </InView>
                );
              }}
            </StandardDataContext.Consumer>
          </>
        )}
      </MatchHistoryProvider>
    </>
  );
};

export const MatchCard = ({ match }: { match: Match }) => (
  <div style={{
    border: "1px solid rgba(0, 0, 0, 0.125)",
    padding: "1.25rem",
    borderRadius: "0.5rem",
  }}>
    <div style={{
      textAlign: "center",
    }}>
      <Team team={match.winners} /> wins against <Team team={match.losers} />
    </div>

    <div style={{
      display: "grid",
      gridTemplateColumns: "auto auto auto auto 1fr auto",
    }}>
      <Breaker />
      {match.winners.map(participant => <TrueskillChanges key={participant.name} participant={participant} />)}
      <Breaker />
      {match.losers.map(participant => <TrueskillChanges key={participant.name} participant={participant} />)}
    </div>
  </div>
);

const Breaker = () => (
  <div style={{
    margin: "0.75rem 0",
    borderTop: "1px solid rgba(0, 0, 0, 0.125)",
    gridColumn: "1 / -1"
  }} />
);

const Mono = ({ children }: { children: ReactNode }) => (
  <span style={{
    fontFamily: "Roboto Mono, monospace",
    fontStyle: "normal",
    color: "rgb(24, 24, 24)",
  }}>
    {children}
  </span>
);

const PlayerName = ({ name }: { name: string}) => (
  <Link to={`/player/${name}`}><Mono>{name}</Mono></Link>
);

const TrueskillChanges = ({ participant }: { participant: MatchParticipant }) => (
  <>
    <div style={{
      marginRight: "0.75rem",
    }}>
      <PlayerName name={participant.name} />
    </div>

    <Mono>
      {participant.skillBefore.trueskill},{participant.skillBefore.deviation}
    </Mono>

    <div style={{
      margin: "0 0.25rem",
      marginTop: "-0.1rem",
    }}>
      <ArrowRightIcon verticalAlign="middle" size={16} />
    </div>

    <Mono>
      {participant.skillAfter.trueskill},{participant.skillAfter.deviation}
    </Mono>

    <div style={{
      textAlign: "right",
    }}>
      <Difference newValue={participant.skillAfter.trueskill} oldValue={participant.skillBefore.trueskill} />
    </div>
    <div style={{
      textAlign: "right",
      marginLeft: "1rem",
    }}>
      <Difference newValue={participant.skillAfter.deviation} oldValue={participant.skillBefore.deviation} />
    </div>
  </>
);

const Difference = ({ newValue, oldValue }: { newValue: number, oldValue: number }) => {
  const difference = newValue - oldValue;

  let text = "–";
  let color = undefined;

  if (difference < 0) {
    text = difference.toString();
    color = "#d6390d";
  } else if (difference > 0) {
    text = "+" + difference;
    color = "#2b822f";
  }

  return (
    <Mono>
      <span style={{ color }}>
        {text}
      </span>
    </Mono>
  );
};

const Team = ({ team }: { team: MatchParticipant[] }) => {
  const elements: ReactNode[] = [];

  for (let i = 0; i < team.length; i++) {
    if (i !== 0) {
      elements.push(<>, </>);
    }

    elements.push(<PlayerName name={team[i].name} />);
  }

  return <>{elements}</>;
};

export default History;
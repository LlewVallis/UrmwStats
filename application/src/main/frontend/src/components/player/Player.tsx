import React from "react";
import { Spinner } from "react-bootstrap";
import { useParams } from "react-router-dom";
import { StandardDataContext } from "../App";
import MatchHistoryProvider from "../MatchHistoryProvider";
import PlayerFigures from "./PlayerFigures";
import PlayerHistory from "./PlayerHistory";
import WinRateGraphs from "./WinRateGraphs";

const Player = () => {
  const { name } = useParams() as any;
  
  return (
    <div style={{
      textAlign: "center",
    }}>
      <StandardDataContext.Consumer>
        { data => {
          const player = data.players.find(player => player.name === name);

          if (player) {
            return (
              <>
                <Title name={name} />
                <Breaker />

                <MatchHistoryProvider player={name}>
                  { history => {
                    if (history === null) {
                      return (
                        <div style={{
                          color: "gray",
                        }}>
                          <Spinner animation="border" /> 
                          <br /> Loading history...
                          <Breaker />
                        </div>
                      );
                    } else if (history.length > 2) {
                      return (
                        <>
                          <PlayerHistory player={player} history={history} />
                          <Breaker />
                        </>
                      );
                    } else {
                      return null;
                    }
                  }}
                </MatchHistoryProvider>

                <PlayerFigures player={player} />
                <WinRateGraphs player={player} />
              </>
            );
          } else {
            return (
              <>
                <h1>The player {name} does not exist</h1>
                Try searching again. 
                If the problem persists contact <code>Llew Vallis#5734</code> on Discord.
              </>
            );
          }
        }}
      </StandardDataContext.Consumer>
    </div>
  );
};

const Title = ({ name }: { name: string }) => {
  return (
    <>
      <h1
        style={{
          display: "inline-block",
          cursor: name === "UnWin" ? "pointer" : undefined,
        }}
        onClick={() => {
          if (name === "UnWin") {
            window.open("/api/doggos/random");
          }
        }}
      >
        Stats for {name}
      </h1>
    </>
  );
};

const Breaker = () => <div style={{ height: "3rem" }} />;

export default Player;
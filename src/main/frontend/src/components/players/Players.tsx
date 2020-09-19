import React, { ReactNode } from "react";
import { useHistory } from "react-router-dom";
import { Card } from "react-bootstrap";
import { GraphIcon } from "@primer/octicons-react";
import { StandardDataContext } from "../App";
import * as playerFigures from "../player/PlayerFigures";
import { Player } from "../../api/player";

const Players = () => (
  <StandardDataContext.Consumer>
    { data => {
      const ranks: { name: string, players: Player[] }[] = [];

      for (const player of data.players) {
        let rank = ranks.find(rank => rank.name === player.rankName);
        if (!rank) {
          rank = { name: player.rankName, players: [] };
          ranks.push(rank);
        }

        rank.players.push(player);
      }

      return (
        <div style={{
          marginTop: `-${RankMarginTop}`,
        }}>
          { ranks.map(({ name, players }) => <Rank key={name} name={name} players={players} />) }
        </div>
      );
    }}
  </StandardDataContext.Consumer>
);

const RankMarginTop = "5rem";

const Rank = ({ name, players }: { name: string, players: Player[] }) => (
  <>
    <h1 style={{
      textAlign: "center",
      textTransform: "capitalize",
      marginTop: RankMarginTop,
    }}>
      {name}
    </h1>

    <div style={{
      display: "flex",
      justifyContent: "center",
      flexWrap: "wrap",
    }}>
      { players.map(player => <PlayerCard key={player.name} player={player} />) }
    </div>
  </>
);

const PlayerCard = ({ player }: { player: Player }) => {
  const history = useHistory();

  return (
    <Card 
      body
      style={{
        width: "13.5rem",
        margin: "0.5rem",
        cursor: "pointer",
      }}
      onClick={() => {
        history.push(`/player/${player.name}`);
      }}
    >
      <Card.Title style={{
        fontSize: "1.15rem",
      }}>
        {player.name}
      </Card.Title>

      <div style={{
        display: "grid",
        gridTemplateColumns: "auto 1fr",
        marginTop: "1rem",
      }}>
        <span>Ranking</span>
        <span style={{
          textAlign: "right",
        }}>
          {playerFigures.rankingString(player.ranking)}
        </span>

        <span>Trueskill</span>
        <span style={{
          textAlign: "right",
        }}>
          <Mono>{player.skill.trueskill}, {player.skill.deviation}</Mono>
        </span>
      </div>

      <div 
        className="display-on-hover"
        style={{
          opacity: "0%",
          position: "absolute",
          left: "0",
          top: "0",
          width: "100%",
          height: "100%",
        }}
      >
        <div style={{
          position: "absolute",
          right: "1rem",
          top: "1rem",
        }}>
          <GraphIcon />
        </div>
      </div>
    </Card>
  );
};

const Mono = ({ children }: { children: ReactNode }) => (
  <span style={{
    fontFamily: "Roboto Mono, monospace",
  }}>
    {children}
  </span>
);

export default Players;
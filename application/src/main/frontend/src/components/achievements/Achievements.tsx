import React, { ReactNode } from "react";
import { Link } from "react-router-dom";
import { Card } from "react-bootstrap";
import { StandardDataContext } from "../App";
import { Achievement } from "../../api/achievement";

const Achievements = () => (
  <StandardDataContext.Consumer>
    { data => {
      const achievements = [...data.achievements];
      achievements.sort((a, b) => b.playersCompleted.length - a.playersCompleted.length);

      return (
        <>
          <h1 style={{
            textAlign: "center",
          }}>
            Achievements
          </h1>

          <div style={{
            display: "flex",
            justifyContent: "center",
            flexWrap: "wrap",
          }}>
            { achievements.map(achievement => <AchievementCard key={achievement.name} achievement={achievement} />) }
          </div>
        </>
      );
    }}
  </StandardDataContext.Consumer>
);

export const AchievementCard = ({ achievement }: { achievement: Achievement }) => (
  <Card 
    body
    style={{
      width: "20rem",
      margin: "0.5rem",
    }}
  >
    <div style={{
      display: "flex",
      flexDirection: "column",
      height: "100%",
    }}>
      <Card.Title style={{
        fontSize: "1.15rem",
      }}>
        {achievement.name}
      </Card.Title>

      {achievement.description || <i>This achievement prefers to maintain an air of mystery...</i>}

      {achievement.playersCompleted.length > 0 ? (
        <>
          <div style={{ flexGrow: 1 }} />

          <div style={{
            margin: "0.75rem 0",
            borderTop: "1px solid rgba(0, 0, 0, 0.125)",
          }} />

          <PlayerList playerNames={achievement.playersCompleted} />
        </>
      ) : null}
    </div>
  </Card>
);

const PlayerList = ({ playerNames }: { playerNames: string[] }) => {
  const elements: ReactNode[] = [];

  for (let i = 0; i < playerNames.length; i++) {
    if (i !== 0) {
      elements.push(
        <span style={{
          marginRight: "0.25rem",
        }}>, </span>
      );
    }

    elements.push(<PlayerName key={playerNames[i]} name={playerNames[i]} />);
  }

  return <div style={{
    textAlign: "center",
  }}>
    {elements}
  </div>;
};

const PlayerName = ({ name }: { name: string}) => (
  <Link to={`/player/${name}`}>
    <span style={{
      fontFamily: "Roboto Mono, monospace",
      fontStyle: "normal",
      color: "rgb(24, 24, 24)",
    }}>
      {name}
    </span>
  </Link>
);

export default Achievements;
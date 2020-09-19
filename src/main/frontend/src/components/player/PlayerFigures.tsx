import React, { ReactNode } from "react";
import { Table } from "react-bootstrap";
import { Player } from "../../api/player";

const PlayerFigures = ({ player }: { player: Player }) => (
  <div style={{
    display: "flex",
    flexWrap: "wrap",
    justifyContent: "center",
  }}>
    <FigureSet name="Trueskill">
      <Figure name="Trueskill" value={player.skill.trueskill} />
      <Figure name="Rating deviation" value={player.skill.deviation} />
      <Figure name="Peak trueskill" value={player.peakSkill.trueskill} />
      <Figure name="Rank" value={player.rankName} />
      <Figure name="Ranking" value={rankingString(player.ranking)} />
    </FigureSet>
    <FigureSet name="Matches">
      <Figure name="Wins" value={player.wins} />
      <Figure name="Losses" value={player.losses} />
      <Streak value={player.streak} />
      <BlankFigure />
      <BlankFigure />
    </FigureSet>
    <FigureSet name="Tourneys">
      <Figure name="Times placed first" value={player.timesPlacedFirst} />
      <Figure name="Times placed second" value={player.timesPlacedSecond} />
      <Figure name="Times placed third" value={player.timesPlacedThird} />
      <BlankFigure />
      <BlankFigure />
    </FigureSet>
  </div>
);

const FigureSet = ({ children, name }: { children: ReactNode, name: string }) => (
  <Table
    bordered striped
    style={{
      width: "20rem",
      margin: "0.75rem",
    }}
  >
    <thead>
      <tr>
        <th colSpan={2}>{name}</th>
      </tr>
    </thead>
    <tbody>
      {children}
    </tbody>
  </Table>
)

const Figure = ({ name, value }: { name: string, value: Object }) => (
  <tr>
    <td style={{
      textAlign: "left",
      lineHeight: "1.2rem",
    }}>
      {name}
    </td>
    
    <td style={{
      textAlign: "right",
      textTransform: "capitalize",
      lineHeight: "1.2rem",
    }}>
      {value.toString()}
    </td>
  </tr>
)

const BlankFigure = () => (
  <tr>
    <td colSpan={2}>
      <div style={{
        height: "1.2rem",
      }} />
    </td>
  </tr>
);

const Streak = ({ value }: { value: number }) => {
  if (value > 0) {
    return <Figure name="Win streak" value={value} />
  } else if (value < 0) {
    return <Figure name="Loss streak" value={-value} />
  } else {
    return <BlankFigure />;
  }
};

export function rankingString(value: number) {
  let result = (value + 1).toString();

  if (result.length === 2 && result.startsWith("1")) {
    result += "th"
  } else if (result.endsWith("1")) {
    result += "st"
  } else if (result.endsWith("2")) {
    result += "nd"
  } else if (result.endsWith("3")) {
    result += "rd"
  } else {
    result += "th"
  }

  return result;
}

export default PlayerFigures;
import React, { ReactNode } from "react";
import Leaderboard from "./Leaderboard"
import Podium, { TourneyDate } from "./Podium"
import TourneyWinRates from "./TourneyWinRates";

const Home = () => (
  <div style={{
    textAlign: "center",
  }}>
    <h1>Leaderboard</h1>
    <Leaderboard />

    <Breaker />

    <FlexContainer>
      <FlexElement align={"flex-start"}>
        <h1>Last tourney placings</h1>
      </FlexElement>
      <FlexElement align={"flex-start"}>
        <h1>Tourney win rates</h1>
      </FlexElement>
    </FlexContainer>
    <FlexContainer>
      <FlexElement align={"center"}>
        <Podium />
      </FlexElement>
      <FlexElement align={"center"}>
        <TourneyWinRates />
      </FlexElement>
    </FlexContainer>
    <FlexContainer>
      <FlexElement align={"flex-end"}>
        <Note>Tourney on <TourneyDate /></Note>
      </FlexElement>
      <FlexElement align={"flex-end"}>
        <Note>Winning in a team awards fractional points</Note>
      </FlexElement>
    </FlexContainer>
  </div>
);

const FlexContainer = ({ children }: { children: ReactNode }) => (
  <div style={{
    display: "flex",
    width: "70rem",
    margin: "0 auto",
  }}>
    {children}
  </div>
);

const FlexElement = ({ align, children }: { align: string, children: ReactNode }) => (
  <div style={{
    alignSelf: align,
    width: "35rem",
  }}>
    {children}
  </div>
);

const Note = ({ children }: { children: ReactNode }) => (
  <i style={{
    marginTop: "1rem",
    display: "block",
  }}>
    {children}
  </i>
);

const Breaker = () => (
  <div style={{
    height: "6rem",
  }} />
);

export default Home;
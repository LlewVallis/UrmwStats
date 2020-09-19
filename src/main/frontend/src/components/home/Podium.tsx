import React from "react";
import { StandardDataContext } from "../App";

const Podium = () => (
  <StandardDataContext.Consumer> 
    { data => {
      const { first, second, third } = data.lastTourney;

      return (
        <div style ={{
          display: "inline-block",
          textAlign: "center",
          width: "35rem",
        }}>
          <div style={{
            display: "flex",
            alignItems: "end",
            justifyContent: "center",
          }}>
            <PodiumPole names={second} placing="Second" variant={"secondary"} height="17.5rem" />
            <PodiumPole names={first} placing="First" variant={"primary"} height="25rem" />
            <PodiumPole names={third} placing="Third" variant={"tertiary"} height="10rem" />
          </div>
        </div>
      );
    }}
  </StandardDataContext.Consumer>
);

export const TourneyDate = () => ( 
   <StandardDataContext.Consumer> 
    { data => {
      const date = new Date(data.lastTourney.timestamp);
      const dateString = `${date.getDate()}/${date.getMonth()}/${date.getFullYear()}`;
      return <>{dateString}</>
    }}
  </StandardDataContext.Consumer>
);

interface PodiumPoleProps {
  names: string[];
  placing: string;
  variant: string;
  height: string;
}

const PodiumPole = ({ names , placing, variant, height }: PodiumPoleProps) => (
  <div style={{
    width: "30%",
    margin: "0 1.5%",
  }}>
    <div style={{
      fontWeight: "bold",
      fontSize: "110%",
      margin: "0 5%",
      textDecoration: "underline",
    }}>
      {names.join(", ")}
    </div>
    <div style={{
      fontSize: "90%",
    }}>
      {placing}
    </div>
    <div 
      className={`background-${variant}`}
      style={{
        height: height,
        marginTop: "0.5rem",
        borderRadius: "0.75rem",
      }}
    />
  </div>
);

export default Podium;
import React, { useState, useEffect } from "react";
import { StandardDataContext } from "./App";

const Footer = () => (
  <StandardDataContext.Consumer>
    { data => (
      <div style={{
        marginTop: "10rem",
        textAlign: "center",
      }}>
        <div style={{
          color: "#555",
          display: "inline-block",
          borderTop: "1px solid",
          width: "30rem",
        }}>
          Please send any inquries to <code>Llew Vallis#5734</code> on Discord
          <br /> Tracking {data.info.playerCount} players, {data.info.matchCount} matches and {data.info.tourneyCount} tourneys
          <br /> Site last updated <UpdatingTime dateString={data.info.lastUpdated}/> ago
        </div>
      </div>
    )}
  </StandardDataContext.Consumer>
);

const UpdatingTime = ({ dateString }: { dateString: string }) => {
  const [timeString, setTimeString] = useState(timeEllapsedString(dateString));

  useEffect(() => {
    const interval = setInterval(() => setTimeString(timeEllapsedString(dateString)), 1000);
    return () => clearInterval(interval);
  });

  return <>{timeString}</>;
};

function timeEllapsedString(dateString: string) {
  const date = new Date(dateString);
  const secondsEllapsed = Math.floor((new Date().getTime() - date.getTime()) / 1000);
  
  const daysEllapsed = secondsEllapsed / 86400;
  if (daysEllapsed > 1) {
    return Math.floor(daysEllapsed) + " day(s)";
  }

  const hoursEllapsed = secondsEllapsed / 3600;
  if (hoursEllapsed > 1) {
    return Math.floor(hoursEllapsed) + " hour(s)";
  }

  const minutesEllapsed = secondsEllapsed / 60;
  if (minutesEllapsed > 1) {
    return Math.floor(minutesEllapsed) + " minute(s)";
  }

  return Math.floor(secondsEllapsed) + " second(s)";
}

export default Footer;
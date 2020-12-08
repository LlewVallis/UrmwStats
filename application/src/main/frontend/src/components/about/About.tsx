import React, { ReactNode, useEffect, useState } from "react";
import JSONTree from "react-json-tree";
import { requestJson } from "../../api/api";
import { StandardDataContext } from "../App";

const About = () => (
  <StandardDataContext.Consumer>
    { data => {
      const lastUpdated = data.info.lastUpdated;

      return (
        <>
          <h1>About</h1>
          <p>
            URMW Stats is a site dedicated to tracking statistics from ranked Missile Wars matches and tournaments.
            All data found here is updated live - you shouldn't even need to refresh your browser.
            For feature requests and bug reports, contact <code>Llew Vallis#5734</code> on Discord, or submit an issue to the GitHub repository.
          </p>

          <h1 style={{
            marginTop: "5rem",
          }}>
            API
          </h1>
          <p>
            URMW Stats provides an API that can be freely used to fetch player, match and tourney statistics in your own software.
            There are no rate limits on this API, but you should be caching as much as possible as a courtesy to other users.
            All API endpoints should be prefixed with <Mono>/api</Mono>.
          </p>

          <Route title="/info" lastUpdated={lastUpdated}>
            Provides generic information about the state of the service.
            This most notably includes <Mono>lastUpdated</Mono> which can be used to determine if other data needs to be refreshed.
          </Route>

          <Route title="/standard-data" lastUpdated={lastUpdated}>
            A richer version of <Mono>/info</Mono> which additionally returns achievements, data for all players and data for the last tourney.
            This essentially packages <Mono>/info</Mono>, <Mono>/achievements</Mono>, <Mono>/players</Mono> and <Mono>/tourneys?count=1</Mono> into a single request.
          </Route>

          <Route title="/players" lastUpdated={lastUpdated}>
            Provides an array containing every player on the leaderboard ordered by their ranking.
            Each player is accompanied by detailed statisitcs.
          </Route>

          <Route title="/achievements" lastUpdated={lastUpdated}>
            Provides an array containing every achievement ordered by their names.
            Each achievement comes with the players that have completed it and its description, or <Mono>null</Mono> if it is a secret achievement.
          </Route>

          <Route title="/renames" lastUpdated={lastUpdated}>
            Provides an object which maps discarded player names to new ones.
            Discarded player names are never returned by this API, so this is unlikely to be of much use.
          </Route>

          <Route title="/tourneys/recent?count=<count>" endpoint="/api/tourneys/recent?count=5" lastUpdated={lastUpdated}>
            Provides information on the last <Mono>&lt;count&gt;</Mono> tourneys, sorted from most recent to least recent.
            The ID of a tourney indicates how many prior tourneys have occured, so the tourney with ID <Mono>2</Mono> would be the third tourney in the season.
            If <Mono>&lt;count&gt;</Mono> is greater than the number of tourneys in the season, only the available number tourneys will be returned.
          </Route>

          <Route title="/matches/recent?count=<count>" endpoint="/api/matches/recent?count=5" lastUpdated={lastUpdated}>
            Provides information on the last <Mono>&lt;count&gt;</Mono> matches, sorted from most recent to least recent.
            The ID of a match indicates how many prior matches have occured, so the match with ID <Mono>2</Mono> would be the third match in the season.
            If <Mono>&lt;count&gt;</Mono> is greater than the number of matches in the season, only the available number matches will be returned.
          </Route>

          <Route title="/doggos/random" lastUpdated={lastUpdated} noSample>
            Provides a random doggo image.
          </Route>
        </>
      );
    }}
  </StandardDataContext.Consumer>
);


const JsonTreeTheme = {
  scheme: "monokai",
  base00: "#272822",
  base01: "#383830",
  base02: "#49483e",
  base03: "#75715e",
  base04: "#a59f85",
  base05: "#f8f8f2",
  base06: "#f5f4f1",
  base07: "#f9f8f5",
  base08: "#f92672",
  base09: "#fd971f",
  base0A: "#f4bf75",
  base0B: "#a6e22e",
  base0C: "#a1efe4",
  base0D: "#66d9ef",
  base0E: "#ae81ff",
  base0F: "#cc6633",
}

interface RouteProps {
  lastUpdated: string;
  title: string;
  endpoint?: string;
  children: ReactNode;
  noSample?: boolean;
}

const Route = ({ lastUpdated, title, children, endpoint, noSample }: RouteProps) => {
  const [sample, setSample] = useState<any>("Fetching sample...");

  useEffect(() => {
    const interval = setInterval(() => fetch(), 15000);

    function fetch() {
      if (noSample) {
        clearInterval(interval);
        return;
      }

      requestJson(endpoint || `/api${title}`).then(value => {
        setSample(value);
        clearInterval(interval);
      }).catch(error => {
        console.error(`Failed to fetch sample for ${endpoint}`, error);
      });
    }

    fetch();
    return () => clearInterval(interval);
  }, [title, endpoint, lastUpdated, noSample]);

  return (
    <>
      <div style={{
        fontFamily: "Roboto Mono, monospace",
        fontSize: "150%",
        fontWeight: "bold",
        marginTop: "3rem",
        marginBottom: "1rem",
      }}>
        {title}
      </div>
      <p style={{ }}>
        {children}
      </p>
      <div 
        className="json-sample"
        style={{
          border: "1px solid rgba(0, 0, 0, 0.125)",
          padding: "1rem",
          borderRadius: "0.5rem",
          display: noSample ? "none" : undefined,
        }}
      >
        <JSONTree data={sample} theme={JsonTreeTheme} keyPath={["sample response"]} shouldExpandNode={() => false} />
      </div>
    </>
  );
};

const Mono = ({ children }: { children: ReactNode }) => (
  <span style={{
    fontFamily: "Roboto Mono, monospace",
  }}>
    {children}
  </span>
);

export default About;
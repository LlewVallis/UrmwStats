import React, { ReactNode, useState, useEffect } from "react";

import { StandardDataContext, } from "./App";
import { StandardData } from "../api/standard-data";
import { Match } from "../api/match";
import * as match from "../api/match";

export interface HistoryProviderProps {
  count?: number;
  player?: string;
  children: (history: Match[] | null) => ReactNode;
}

const MatchHistoryProvider = ({ count, player, children }: HistoryProviderProps) => (
  <StandardDataContext.Consumer>
    { data => <MatchHistoryWithData count={count} player={player} data={data}>{children}</MatchHistoryWithData> }
  </StandardDataContext.Consumer>
);

export interface HistoryProviderWithDataProps {
  count?: number;
  player?: string;
  children: (history: Match[] | null) => ReactNode;
  data: StandardData;
}

const MatchHistoryWithData = ({ count, player, children, data }: HistoryProviderWithDataProps) => {
  const [ history, setHistory ] = useState<Match[] | null>(null);

  useEffect(() => {
    const interval = setInterval(() => fetch(), 15000);

    function fetch() {
      match.fetch(count || 10000, player).then(history => {
        setHistory(history)
        clearInterval(interval);
      }).catch(error => {
        console.error("Failed to fetch history", error);
      });
    }

    fetch();
    return () => clearInterval(interval);
  }, [count, player, data]);

  return <>{children(history)}</>;
}

export default MatchHistoryProvider;
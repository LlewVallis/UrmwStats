import { requestJson } from "./api";

export interface Info {
  lastUpdated: string;
  playerCount: number;
  matchCount: number;
  tourneyCount: number;
  achievementCount: number;
  trueskillSettings: TrueskillSettings;
}

export interface TrueskillSettings {
  mu: number;
  sigma: number;
  beta: number;
  tau: number;
  drawProbability: number;
}

export function fetch(): Promise<Info> {
  return requestJson("/api/info");
}

export function queryHasChanged(info: Info): Promise<boolean> {
  return fetch().then(newInfo => new Date(newInfo.lastUpdated).getTime() !== new Date(info.lastUpdated).getTime());
}
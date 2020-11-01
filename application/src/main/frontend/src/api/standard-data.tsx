import { requestJson } from "./api";

import { Info } from "./info";
import { Player } from "./player";
import { Tourney } from "./tourney";

export interface StandardData {
  info: Info;
  players: Player[];
  lastTourney: Tourney;
}

export function fetch(): Promise<StandardData> {
  return requestJson("/api/standard-data");
}
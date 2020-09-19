import { requestJson } from "./api";
import Skill from "./skill";

export interface Match {
  id: number;
  winners: MatchParticipant[];
  losers: MatchParticipant[];
  timestamp: string;
}

export interface MatchParticipant {
  name: string;
  skillBefore: Skill;
  skillAfter: Skill;
}

export function fetch(count: number, filter?: string): Promise<Match[]> {
  return requestJson(`/api/matches/recent?count=${count}${filter ? `&filter=${filter}` : ""}`);
}
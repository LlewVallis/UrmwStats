import Skill from "./skill";
import { requestJson } from "./api";

export interface Player {
  name: string;
  skill: Skill;
  peakSkill: Skill;
  ranking: number;
  wins: number;
  losses: number;
  fractionalTourneyWins: number;
  timesPlacedFirst: number;
  timesPlacedSecond: number;
  timesPlacedThird: number;
  winsAgainst: Record<string, number>;
  lossesAgainst: Record<string, number>;
  rankName: string;
  streak: number;
  completedAchievements: string[];
}

export function fetch(): Promise<Player[]> {
  return requestJson("/api/players");
};

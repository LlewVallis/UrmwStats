import { requestJson } from "./api";

export interface Achievement {
  name: string;
  description: string | null;
  playersCompleted: string[];
}

export function fetch(): Promise<Achievement[]> {
  return requestJson("/api/achievement");
};
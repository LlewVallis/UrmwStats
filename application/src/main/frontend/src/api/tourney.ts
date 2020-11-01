import { requestJson } from "./api";

export interface Tourney {
  id: number;
  first: string[];
  second: string[];
  third: string[];
  timestamp: string;
}

export function fetch(count: number, filter?: string): Promise<Tourney[]> {
  return requestJson(`/api/tourneys/recent?count=${count}${filter ? `&filter=${filter}` : ""}`);
}
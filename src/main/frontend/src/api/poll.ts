import { requestJson, request } from "./api";

export interface Poll {
  name: string;
  options: string[];
  voters: Voter[];
  winningOptions: string[];
}

export interface Voter {
  id: string;
  name: string;
  preferences: number[];
}

export function fetch(): Promise<Poll[]> {
  return requestJson("/api/staff/polls");
}

export function create(name: string, options: string[]): Promise<Poll> {
  return requestJson(`/api/staff/poll/${encodeURIComponent(name)}`, {
    method: "POST",
    body: JSON.stringify({ options }),
    headers: {
      "Content-Type": "application/json",
    },
  });
}

export function close(name: string): Promise<void> {
  return request(`/api/staff/poll/${encodeURIComponent(name)}`, {
    method: "DELETE",
  }).then(() => {});
}

export function vote(name: string, preferences: number[]): Promise<Poll> {
  return requestJson(`/api/staff/poll/${encodeURIComponent(name)}/vote`, {
    method: "POST",
    body: JSON.stringify({ preferences }),
    headers: {
      "Content-Type": "application/json",
    },
  });
}

export function withdraw(name: string): Promise<Poll> {
  return requestJson(`/api/staff/poll/${encodeURIComponent(name)}/vote`, {
    method: "DELETE",
  });
}
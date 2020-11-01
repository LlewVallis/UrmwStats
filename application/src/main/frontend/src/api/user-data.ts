import { requestJson } from "./api";

export interface UserData {
  name: string;
  avatarUri: string;
}

export function fetch(ids: string[]): Promise<Record<string, UserData>> {
  return requestJson("/api/fetch-users", {
    method: "POST",
    body: JSON.stringify(ids),
    headers: {
      "Content-Type": "application/json",
    },
  });
}
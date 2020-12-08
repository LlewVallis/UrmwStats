import { request, requestJson } from "./api";

export interface LoginDetails {
  id: string,
  name: string,
  discriminator: string,
  avatarUri: string,
  staff: boolean,
}

const loginDetails: Promise<LoginDetails> = requestJson("/api/discord-user").catch(() => null);

export default loginDetails;

export function logout() {
  request("/logout", {
     method: "POST",
  }).then(() => {
    window.location.href = "/";
  }).catch(() => {
    alert("You could not be logged out");
  });
}
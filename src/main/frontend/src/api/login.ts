import Cookies from "js-cookie";

import { request, requestJson } from "./api";

export interface LoginDetails {
  id: string,
  name: string,
  discriminator: string,
  avatarUri: string,
}

const loginDetails: Promise<LoginDetails> = requestJson("/api/discord-user");

export default loginDetails;

export function logout() {
  request("/logout", {
     method: "POST",
     headers: new Headers({
        "X-XSRF-TOKEN": Cookies.get("XSRF-TOKEN") || "",
     }),
  }).then(() => {
    window.location.href = "/";
  }).catch(() => {
    alert("You could not be logged out");
  });
}
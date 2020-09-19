import React from "react";

export interface LoginButtonProps {
  disabled?: boolean,
}

const LoginButton = () => (
  <a
    href="/oauth2/authorization/discord"
    className="text-white"
    style={{
      textAlign: "center",
    }}
  >
    Login with Discord
  </a>
);

export default LoginButton;
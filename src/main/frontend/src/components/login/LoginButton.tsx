import React from "react";

export interface LoginButtonProps {
  disabled?: boolean,
}

const LoginButton = () => (
  <a
    href="/login"
    className="text-white"
    style={{
      textAlign: "center",
    }}
  >
    Login with Discord
  </a>
);

export default LoginButton;
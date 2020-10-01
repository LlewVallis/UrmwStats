import React, { ReactNode } from "react";
import { Link, useHistory } from "react-router-dom";
import { Navbar } from "react-bootstrap";
import { AppState, StandardDataContext } from "./App";
import LoginWidget from "./login/LoginWidget";
import PlayerSearch from "./PlayerSearch";

const Header = ({ state }: { state: AppState }) => {
  const history = useHistory();

  return (
    <Navbar bg="primary">
      <Navbar.Brand>
        <Link
          to="/"
          style={{
            height: "4rem",
            margin: "0.75rem 1.5rem 0 0",
            fontSize: "2rem",
            fontFamily: "Righteous, monospace",
            color: "white",
            textDecoration: "none",
          }}
        >
          URMW Stats
        </Link>
      </Navbar.Brand>

      <div style={{
        flexShrink: 1,
        overflow: "scroll",
      }}>
        <Page location="/about">About</Page>
        <Page location="/players">Players</Page>
        <Page location="/history">History</Page>
        <Page location="/calculator">Calculator</Page>
      </div>

      <div className="ml-auto">
        <LoginWidget />
      </div>

      {state.data ? (
        <div style={{
          marginLeft: "2rem",
        }}>
          <StandardDataContext.Provider value={state.data}>
            <PlayerSearch placeholder="Search players" callback={name => history.push(`/player/${name}`)} />
          </StandardDataContext.Provider>
        </div>
      ) : null}
    </Navbar>
  );
};

const Page = ({ children, location }: { children: ReactNode, location: string }) => (
  <Link 
    to={location}
    className="text-white"
    style={{
      fontSize: "110%",
      fontWeight: "bold",
      margin: "0 1rem",
    }}
  >
    {children}
  </Link>
);

export default Header;
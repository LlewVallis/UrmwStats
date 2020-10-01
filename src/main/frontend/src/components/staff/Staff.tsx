import React, { useEffect } from "react";
import PageSpinner from "../PageSpinner";
import PollsProvider from "./PollsProvider";
import loginDetails, { LoginDetails } from "../../api/login";
import { Poll as PollData } from "../../api/poll";
import CreatePollButton from "./CreatePollButton";
import Poll from "./Poll";

const Staff = () => {
  useEffect(() => {
    loginDetails.catch(() => {
      window.location.href = "/oauth2/authorization/discord";
    });
  }, []);

  return (
    <PollsProvider>
      { (polls, loginDetails, errored, refreshNow) => (
        <>
          <div style={{
            display: "flex",
            marginBottom: "2rem",
          }}>
            <h1 style={{
              margin: 0,
            }}>
              Staff
            </h1>

            {polls ? (
              <div style={{
                flexGrow: 1,
                alignSelf: "center",
                textAlign: "right",
              }}>
                <CreatePollButton onCreate={refreshNow} />
              </div>
            ) : null}
          </div>

          {polls ? (
            <PollView polls={polls} loginDetails={loginDetails!} refreshNow={refreshNow} />
          ) : (
            <PageSpinner message="Loading polls..." errored={errored} />
          )}
        </>
      )}
    </PollsProvider>
  );
};

const PollView = ({ polls, loginDetails, refreshNow }: { polls: PollData[], loginDetails: LoginDetails, refreshNow: () => void }) => {
  if (polls.length === 0) {
    return (
      <div style={{
        textAlign: "center",
        margin: "4rem 0",
      }}>
        &mdash; No polls running &mdash;
      </div>
    );
  }

  return (
    <div style={{
      display: "flex",
      flexWrap: "wrap",
      margin: "0 -0.5rem",
    }}>
      {polls.map(poll => <Poll key={poll.name} data={poll} loginDetails={loginDetails} refreshNow={refreshNow} />)}
    </div>
  );
};

export default Staff;
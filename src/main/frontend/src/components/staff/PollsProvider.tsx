import React, { ReactNode, useState, useEffect } from "react";
import { Poll } from "../../api/poll";
import * as poll from "../../api/poll";
import loginDetailsPromise, { LoginDetails } from "../../api/login";

export interface PollProviderProps {
  children: (polls: Poll[] | null, loginDetails: LoginDetails | null, errored: boolean, refreshNow: () => void) => ReactNode;
}

const PollsProvider = ({ children }: PollProviderProps) => {
  const [ polls, setPolls ] = useState<Poll[] | null>(null);
  const [ loginDetails, setLoginDetails ] = useState<LoginDetails | null>(null);
  const [ errored, setErrored ] = useState(false);
  const [ refreshNonce, setRefreshNonce ] = useState(new Date());

  useEffect(() => {
    const interval = setInterval(() => fetch(), 15000);

    function fetch() {
      poll.fetch().then(polls => {
        setPolls(polls)
        setErrored(false);
      }).catch(error => {
          console.error("Failed to fetch polls", error);
          setErrored(true);
      });
    }

    fetch();
    return () => clearInterval(interval);
  }, [refreshNonce]);

  useEffect(() => {
    loginDetailsPromise.then(loginDetails => {
      setLoginDetails(loginDetails);
    });
  });

  const refreshNow = () => {
    setRefreshNonce(new Date());
  };

  return <>
    {(() => {
      if (polls !== null && loginDetails !== null) {
        return children(polls, loginDetails, errored, refreshNow);
      } else {
        return children(null, null, errored, refreshNow);
      }
    })()}
  </>;
}

export default PollsProvider;
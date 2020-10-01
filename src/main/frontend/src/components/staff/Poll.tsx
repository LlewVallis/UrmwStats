import { ArrowRightIcon } from "@primer/octicons-react";
import React, { ReactNode, useState } from "react";
import { Button, Modal, OverlayTrigger, Popover } from "react-bootstrap";
import { Poll as PollData, Voter as VoterData } from "../../api/poll";
import * as poll from "../../api/poll";
import VoteButton from "./VoteButton";
import { LoginDetails } from "../../api/login";

const Poll = ({ data, loginDetails, refreshNow }: { data: PollData, loginDetails: LoginDetails, refreshNow: () => void }) => {
  const hasVoted = data.voters.find(voter => voter.id === loginDetails.id) !== undefined;

  return (
    <div style={{
      display: "flex",
      flexDirection: "column",
      border: "1px solid rgba(0, 0, 0, 0.125)",
      padding: "1.25rem",
      borderRadius: "0.5rem",
      flexGrow: 1,
      margin: "0 0.5rem",
      marginBottom: "0.5rem",
      minWidth: "17.5rem",
    }}>
      <div>
        <b>{data.name}</b>

        {chosenPreferences(data, loginDetails).map(option => {
          const winning = data.winningOptions.includes(option) && data.winningOptions.length < data.options.length;
          const directVotes = data.voters.filter(voter => data.options[voter.preferences[0]] === option).length;

          return (
            <div key={option}>
              <ArrowRightIcon verticalAlign="middle" /> {option}
              {winning ? ", winning" : null}
              {directVotes > 0 ? (
                `${winning ? " with" : ","} ${directVotes} direct vote${directVotes > 1 ? "s" : ""}`
              ) : null}
            </div>
          );
        })}
      </div>

      <div style={{
        flexGrow: 1,
      }} />

      <div style={{
        marginTop: "1rem",
      }}>
        {data.voters.length === 0 ? (
          "No one has voted yet."
        ) : (
          data.voters.map((voter, i) => (
            <>
              {i > 0 ? ", " : null}
              <Voter data={data} key={voter.name} voter={voter} />
            </>
          ))
        )}

        <Breaker />

        <div style={{
          display: "flex",
        }}>
          <VoteButton data={data} loginDetails={loginDetails} onVote={refreshNow} />

          {hasVoted ? (
            <span style={{
              marginLeft: "0.75rem",
            }}>
              <ConfirmingButton
                title="Withdraw vote"
                variant="primary"
                perform={() => poll.withdraw(data.name)}
                onComplete={refreshNow}
              >
                Are you sure you want to withdraw your vote?
              </ConfirmingButton>
            </span>
          ) : null}

          <div style={{
            flexGrow: 1,
          }} />

          <ConfirmingButton
            title="Close poll"
            variant="light"
            perform={() => poll.close(data.name)}
            onComplete={refreshNow}
          >
            Are you sure you want to close "{data.name}"?
          </ConfirmingButton>
        </div>
      </div>
    </div>
  );
}; 

const Voter = ({ data, voter } : { data: PollData, voter: VoterData }) => (
  <OverlayTrigger overlay={(
    <Popover id="voter-popover">
      <Popover.Title>{voter.name}'s votes</Popover.Title>
      <Popover.Content>
        <ol>
          {voter.preferences.map(index => (
            <li key={index}>
              {data.options[index]}
            </li>
          ))}
        </ol>
      </Popover.Content>
    </Popover>
  )}>
    <span className="hover-underline" style={{
      fontFamily: "Roboto Mono, monospace",
      fontStyle: "normal",
      fontSize: "95%",
      color: "rgb(24, 24, 24)",
      cursor: "pointer",
      userSelect: "none",
    }}>
      {voter.name}
    </span>
  </OverlayTrigger>
);

const Breaker = () => (
  <div style={{
    margin: "0.75rem 0",
    borderTop: "1px solid rgba(0, 0, 0, 0.125)",
  }} />
);

interface ConfirmingButtonProps {
  title: string;
  children: ReactNode;
  variant: string;
  perform: () => Promise<any>;
  onComplete: () => void;
};

const ConfirmingButton = ({ title, children, variant, perform, onComplete }: ConfirmingButtonProps) => {
  const [showModal, setShowModal] = useState(false);
  const close = () => setShowModal(false);

  return (
    <>
      <Modal show={showModal} onHide={close}>
        <Modal.Header closeButton>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {children}

          <div style={{
            marginTop: "1rem",
            display: "flex",
          }}>
            <Button onClick={() => {
              perform().then(() => {
                onComplete();
                close();
              }).catch(error => {
                console.error("Failed to perform action for '" + title + "'", error)
              });
            }}>
              Yes
            </Button>

            <div style={{
              flexGrow: 1,
            }} />

            <Button variant="light" onClick={close}>
              Go back
            </Button>
          </div>
        </Modal.Body>
      </Modal>

      <Button variant={variant} onClick={() => setShowModal(true)}>
        {title}
      </Button>
    </>
  );
};

export function chosenPreferences(data: PollData, loginDetails: LoginDetails): string[] {
  const selfVoter = data.voters.find(voter => voter.id === loginDetails.id);
  if (selfVoter) {
    return selfVoter.preferences.map(index => data.options[index]);
  } else {
    return [...data.options];
  }
}

export default Poll;
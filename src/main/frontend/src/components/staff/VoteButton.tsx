import React, { useState } from "react";
import { Modal, Button } from "react-bootstrap";
import { Poll } from "../../api/poll";
import { DragDropContext, Draggable, Droppable, DropResult } from "react-beautiful-dnd";
import * as poll from "../../api/poll";
import { LoginDetails } from "../../api/login";
import { chosenPreferences } from "./Poll";

const VoteButton = ({ data, loginDetails, onVote }: { data: Poll, loginDetails: LoginDetails, onVote: () => void }) => {
  const [preferences, setPreferences] = useState(chosenPreferences(data, loginDetails));
  const [showModal, setShowModal] = useState(false);
  const close = () => setShowModal(false);

  const onDragEnd = ({ destination, source }: DropResult) => {
    if (!destination) {
      return;
    }

    const newPreferences = [...preferences];
    newPreferences.splice(source.index, 1);
    newPreferences.splice(destination.index, 0, preferences[source.index]);

    setPreferences(newPreferences);
  };

  return (
    <>
      <Modal show={showModal} onHide={close}>
        <Modal.Header closeButton>
        <Modal.Title>{data.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <PreferenceLabel text="Most preferred" />
          <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="preferences">
              {(provided) => (
                <div
                  ref={provided.innerRef}
                  {...provided.droppableProps}
                >
                  {preferences.map((preference, i) => (
                    <Draggable key={preference} draggableId={preference} index={i}>
                      {(provided) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          {...provided.dragHandleProps}
                          style={{
                            padding: "0.25rem",
                            ...provided.draggableProps.style,
                          }}
                        >
                          <div style={{
                            border: "1px solid rgba(0, 0, 0, 0.125)",
                            padding: "0.5rem",
                            borderRadius: "0.25rem",
                            backgroundColor: "white",
                          }}>
                            {preference}
                          </div>
                        </div>
                      )}
                    </Draggable>
                  ))}

                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
          <PreferenceLabel text="Least preferred" />

          <div style={{
            marginTop: "2rem",
            display: "flex",
          }}>
            <Button onClick={() => {
              const preferenceIndices = preferences.map(preference => data.options.indexOf(preference));

              poll.vote(data.name, preferenceIndices).then(() => {
                onVote();
                close();
              }).catch(error => {
                console.error("Failed to cast vote", error);
              });
            }}>
              Cast vote
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

      <Button onClick={() => setShowModal(true)}>
        Vote
      </Button>
    </>
  );
};

const PreferenceLabel = ({ text }: { text: string }) => (
  <div style={{
    textAlign: "center",
  }}>
    {text}
  </div>
);

export default VoteButton;
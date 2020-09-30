import { ArrowRightIcon, XIcon } from "@primer/octicons-react";
import React, { useRef, useState } from "react";
import { Modal, Form, Button } from "react-bootstrap";
import * as poll from "../../api/poll";

const CreatePollButton = ({ onCreate }: { onCreate: () => void }) => {
  const [ showModal, setShowModal ] = useState(false);
  const [ options, setOptions ] = useState<string[]>([]);

  const close = () => {
    setShowModal(false);
    setOptions([]);
  }

  const nameInput = useRef<HTMLInputElement | null>(null);
  const optionInput = useRef<HTMLInputElement | null>(null);

  return (
    <>
      <Modal show={showModal} onHide={close}>
        <Modal.Header closeButton>
          <Modal.Title>Create poll</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group>
            <Form.Label>Name</Form.Label>
            <Form.Control ref={nameInput} type="text" placeholder="Enter name" required />
          </Form.Group>
          <Form.Group>
            <Form.Label>Options</Form.Label>

            {options.map(option => (
              <div 
                key={option}
                style={{
                  display: "flex",
                  marginBottom: "0.5rem",
                }}
              >
                <span>
                  <ArrowRightIcon verticalAlign="middle" /> {option}
                </span>
                <div style={{
                  flexGrow: 1,
                  textAlign: "right",
                }}>
                  <span
                    onClick={() => {
                      setOptions(options.filter(other => other !== option));
                    }}
                    style={{
                      cursor: "pointer",
                    }}
                  >
                    <XIcon size={24} />
                  </span>
                </div>
              </div>
            ))}

            <form 
              onSubmit={e => {
                e.preventDefault();

                const value = optionInput.current!.value;
                if (value && !options.includes(value)) {
                  setOptions([...options, value]);
                  optionInput.current!.value = "";
                }
              }}
            >
              <Form.Control ref={optionInput} type="text" placeholder="Add option" />
            </form>
          </Form.Group>

          <Button 
            variant="primary"
            disabled={options.length < 2}
            onClick={() => {
              const name = nameInput.current!.value;
              if (!/\S/.test(name)) {
                return;
              }

              poll.create(name, options).then(() => {
                onCreate();
                close();
              }).catch(error => {
                console.error("Failed to create poll", error);
              });
            }}
          >
            Create poll
          </Button>
        </Modal.Body>
      </Modal>
      <Button variant="primary" onClick={() => setShowModal(true)}>
        Create poll
      </Button>
    </>
  );
};

export default CreatePollButton;
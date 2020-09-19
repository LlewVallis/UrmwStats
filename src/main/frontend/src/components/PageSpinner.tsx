import React from "react";
import { Spinner } from "react-bootstrap";
import { XIcon } from "@primer/octicons-react";

export interface PageSpinnerProps {
  errored: boolean,
}

const PageSpinner = ({ errored }: PageSpinnerProps) => {
  const mainComponent = errored ? <XIcon /> : <Spinner animation="border" />;
  const tooltip = errored ? "Failed to connect, check your connection" : "Loading...";

  const scale = errored ? 3.5 : 2;

  return (
    <div style={{
      color: "gray",
      textAlign: "center",
      margin: "4rem 0",
    }}>
      <div style={{
        height: "4rem",
      }}>
        <div style={{
          display: "inline-block",
          transformOrigin: "top",
          transform: `scale(${scale})`,
        }}>
          {mainComponent}
        </div>
      </div>

      <div style={{
        marginTop: "1rem",
      }}>
        {tooltip}
      </div>
    </div>
  );
};

export default PageSpinner;
import React from "react";
import { LoginDetails, logout } from "../../api/login";
import { Dropdown } from "react-bootstrap";
import { useHistory } from "react-router-dom";

const ProfileWidget = ({ loginDetails }: { loginDetails: LoginDetails }) => (
  <Dropdown drop="left">
    <Dropdown.Toggle as={UnstyledToggle} id="dropdown-custom-components">
      <ProfilePicture loginDetails={loginDetails} />
    </Dropdown.Toggle>

    <Dropdown.Menu>
      {loginDetails.staff ? (
        <StaffButton />
      ) : null}
      <LogoutButton />
    </Dropdown.Menu>
  </Dropdown>
);

const ProfilePicture = ({ loginDetails }: { loginDetails: LoginDetails }) => (
      <img 
        src={loginDetails.avatarUri + "?size=128"}
        alt=""
        className="text-light bg-light"
        style={{
          width: "3rem",
          height: "3rem",
          cursor: "pointer",
          border: "2px solid #666",
          borderRadius: "10px",
        }}
      />
);

const StaffButton = () => {
  const history = useHistory();

  return (
    <Dropdown.Item onClick={() => {
      history.push("/staff");
    }}>
      Staff
    </Dropdown.Item>
  );
};

const LogoutButton = () => (
  <Dropdown.Item onClick={logout}>
    <b>Logout</b>
  </Dropdown.Item>
);

const UnstyledToggle = React.forwardRef(({ children, onClick }: any, ref: any) => (
  <div
    ref={ref}
    onClick={e => {
      e.preventDefault();
      onClick(e);
    }}
  >
    {children}
  </div>
));

export default ProfileWidget;
import React from "react";
import { LoginDetails, logout } from "../../api/login";
import { Dropdown } from "react-bootstrap";

export interface ProfileWidgetProps {
  loginDetails: LoginDetails;
}

const ProfileWidget = ({ loginDetails }: ProfileWidgetProps) => (
  <Dropdown drop="left">
    <Dropdown.Toggle as={UnstyledToggle} id="dropdown-custom-components">
      <ProfilePicture loginDetails={loginDetails} />
    </Dropdown.Toggle>

    <Dropdown.Menu>
      <LogoutButton />
    </Dropdown.Menu>
  </Dropdown>
);

const ProfilePicture = ({ loginDetails }: ProfileWidgetProps) => (
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
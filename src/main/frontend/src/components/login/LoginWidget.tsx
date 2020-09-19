import React, { useEffect, useState } from "react";
import loginDetailsPromise, { LoginDetails } from "../../api/login";
import LoginButton from "./LoginButton";
import ProfileWidget from "./ProfileWidget";

const LoginWidget = () => {
  const [loading, setLoading] = useState(true);
  const [loginDetails, setLoginDetails] = useState<LoginDetails | null>(null);

  useEffect(() => {
    loginDetailsPromise
      .then(setLoginDetails)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <span />;
  } else if (loginDetails === null) {
    return <LoginButton />;
  } else {
    return <ProfileWidget loginDetails={loginDetails} />
  }
};

export default LoginWidget;
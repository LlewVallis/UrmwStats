module.exports = {
  devServer: configFunction => (proxy, allowedHost) => {
    const config = configFunction(proxy, allowedHost);

    config.proxy = {
      "/api": "http://localhost:8080",
      "/oauth2": "http://localhost:8080",
      "/login": "http://localhost:8080",
      "/logout": "http://localhost:8080",
    };

    return config;
  },
};
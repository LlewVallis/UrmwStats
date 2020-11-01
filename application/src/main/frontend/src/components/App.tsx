import React, { Component, ReactNode, Context } from "react";
import { BrowserRouter, Switch, Route } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";

import Header from "./Header";
import Footer from "./Footer";
import PageSpinner from "./PageSpinner";
import { StandardData } from "../api/standard-data";
import * as standardData from "../api/standard-data";
import * as info from "../api/info";

import Home from "./home/Home";
import About from "./about/About";
import Player from "./player/Player";
import History from "./history/History";
import Players from "./players/Players";
import Calculator from "./calculator/Calculator";
import Staff from "./staff/Staff";

import "react-toastify/dist/ReactToastify.css";
import Export from "./export/Export";

export const PrimaryColor = "#663399";
export const SecondaryColor = "#ff4c81";
export const TertiaryColor = "#fc9434";

export interface AppState {
  data?: StandardData;
  errored: boolean;
}

export const StandardDataContext: Context<StandardData> = React.createContext(undefined as any);

export default class App extends Component<{}, AppState> {

  private interval?: NodeJS.Timeout;

  constructor(props: Readonly<{}>) {
    super(props);
    this.state = { errored: false };
  }

  componentDidMount() {
    this.interval = setInterval(() => this.pollApi(), 5000);
    this.pollApi();
  }

  componentWillUnmount() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  }

  pollApi() {
    if (this.state.data) {
      info.queryHasChanged(this.state.data.info).then(hasChanged => {
        if (hasChanged) {
          this.reloadData();
        }
      }).catch(error => {
        if (!this.state.errored) {
          toast.error("Lost connection to URMW Stats");
        }

        console.error("Failed to fetch info", error);
        this.setState({ errored: true })
      });
    } else {
      this.reloadData();
    }
  }

  reloadData() {
    standardData.fetch().then(data => {
      if (this.state.data && this.state.errored) {
        toast.success("Restored connection to URMW Stats");
      }

      this.setState({ data, errored: false });
    }).catch(error => {
      console.error("Failed to fetch standard data", error);
      this.setState({ errored: true })
    })
  }

  render(): ReactNode {
    return (
      <BrowserRouter>
        <ToastContainer />
        <Header state={this.state} />

        <main style={{
          overflowX: "hidden",
        }}>
          {this.renderContent()}
        </main>
      </BrowserRouter>
    );
  }

  renderContent(): ReactNode {
    if (this.state.data) {
      return (
        <StandardDataContext.Provider value={this.state.data}>
          <Switch>
            <Route path="/about">
              <About />
            </Route>
            <Route path="/players">
              <Players />
            </Route>
            <Route path="/history">
              <History />
            </Route>
            <Route path="/calculator">
              <Calculator />
            </Route>
            <Route path="/player/:name">
              <Player />
            </Route>
            <Route path="/export/:channelId/:attachmentId/:fileName">
              <Export />
            </Route>
            <Route path="/staff">
              <Staff />
            </Route>
            <Route path="/">
              <Home />
            </Route>
          </Switch>
          <Footer />
        </StandardDataContext.Provider>
      );
    } else {
      return <PageSpinner errored={this.state.errored} />
    }
  }
}
import React, { useRef } from "react";
import { ReactSearchAutocomplete } from "react-search-autocomplete";
import Fuse from "fuse.js";
import { StandardDataContext } from "./App";

export type Callback = (name: string) => void;

interface Item {
  id: number;
  name: string;
}

const PlayerSearch = ({ callback, placeholder }: { callback: Callback, placeholder: string }) => {
  const formRef = useRef(null);

  return (
    <StandardDataContext.Consumer>
      { data => {
        const items: Item[] = data.players.map((player, i) => ({
          id: i,
          name: player.name,
        }));

        return (
          <form 
            ref={formRef}
            onSubmit={e => {
              e.preventDefault();
              onSubmitForm(callback, items, formRef);
            }}
            style={{
              display: "inline-block",
              width: "10rem",
              height: "3rem",
            }}
          >
            <ReactSearchAutocomplete
              items={items}
              placeholder={placeholder}
              showIcon={false}
              useCaching={false}
              maxResults={5}
              onSelect={(item: Item) => onClickItem(callback, item)}
              fuseOptions={fuseOptions}
              styling={{
                height: "3rem",
                borderRadius: "0.5rem",
                boxShadow: "none",
              }}
            />
          </form>
        );
      }}
    </StandardDataContext.Consumer>
  );
};

const fuseOptions = {
  shouldSort: true,
  threshold: 0.3,
  location: 0,
  distance: 100,
  maxPatternLength: 32,
  minMatchCharLength: 1,
  keys: [
    "name",
  ],
};

type Ref<T> = React.MutableRefObject<T | null>;

function onClickItem(callback: Callback, item: Item) {
  callback(item.name);
}

function onSubmitForm(callback: Callback, items: Item[], formRef: Ref<HTMLFormElement>) {
  const inputElement = formRef.current!.querySelector("input") as HTMLInputElement;
  const fuzzyName = inputElement.value.toLowerCase();

  const fuse = new Fuse(items, fuseOptions);
  const match = fuse.search(fuzzyName)[0];

  if (match) {
    callback(match.item.name);
  }
}

export default PlayerSearch; 
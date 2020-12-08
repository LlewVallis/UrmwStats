import React, { ReactChild, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageSpinner from "../PageSpinner";
import { UserData } from "../../api/user-data";
import discordIcon from "./discord-icon.svg";
import { Button } from "react-bootstrap";
import { DownloadIcon, BookmarkSlashIcon } from "@primer/octicons-react";
import { Base64 } from "js-base64";
import Twemoji from "react-twemoji";
import * as fileSaver from "file-saver";
import * as discordMarkdown from "discord-markdown";
import * as userData from "../../api/user-data";
import * as pako from "pako";

import "./export.scss";

const ForegroundColor = "#dcddde";
const BackgroundColor = "#36393f";
const BackgroundSecondaryColor = "#2f3136";

interface ExportData {
  timestamp: string;
  channelName: string;
  messages: Message[];
  users: Record<string, UserData>;
  authorNames: Record<string, string>;
}

interface Message {
  flags: number;
  type: number;
  content: string;
  author: string;
  timestamp: string;
  embeds: Embed[];
  attachments: Attachment[];
  reactions: Reaction[];
}

interface Embed {
  author: string | null;
  title: string | null;
  description: string | null;
  footer: string | null;
  fields: EmbedField[];
}

interface EmbedField {
  name: string | null;
  value: string | null;
}

interface Attachment {
  name: string;
  content: string | null;
}

interface Reaction {
  name: string;
  count: string;
}

const Export = () => {
  const [data, setData] = useState<ExportData | null>(null);
  const [errored, setErrored] = useState(false);
  const [statusMessage, setStatusMessage] = useState("Downloading attachment...");

  const { channelId, attachmentId, fileName } = useParams() as any;
  const attachmentUrl = `/api/download-attachment/${encodeURIComponent(channelId)}/${encodeURIComponent(attachmentId)}/${encodeURIComponent(fileName)}`;

  useEffect(() => {
    fetch(attachmentUrl)
      .then(async response => {
        const dataBlob = await response.blob();
        const dataBuffer = await dataBlob.arrayBuffer();
        const dataBinary = pako.inflate(new Uint8Array(dataBuffer));
        const dataString = new TextDecoder("UTF-8").decode(dataBinary);
        const data = JSON.parse(dataString) as ExportData;

        const requiredUserIds = [...new Set(data.messages.map(message => message.author))];

        setStatusMessage("Fetching users...");
        const users = await userData.fetch(requiredUserIds);

        setData({ ...data, users });
      })
      .catch(error => {
        console.error(`Failed to load ${attachmentUrl}`, error);
        setErrored(true);
      });
  }, [attachmentUrl]);

  return (
    <div style={{
      color: ForegroundColor,
      backgroundColor: BackgroundColor,
      minHeight: "100vh",
    }}>
      {(() => {
        if (data) {
          const date = new Date(data.timestamp);

          return (
            <div style={{
              padding: "2rem 5rem",
            }}>
              <div style={{
                display: "flex",
              }}>
                <h1 style={{
                  margin: 0,
                }}>
                  Export of #{data.channelName}
                </h1>

                <div style={{
                  flexGrow: 1,
                  alignSelf: "center",
                  textAlign: "right",
                }}>
                  <Button onClick={() => {
                    const dataString = JSON.stringify(data);
                    const dataBlob = new Blob([dataString], { type: "application/json" });
                    const dataUrl = URL.createObjectURL(dataBlob);

                    try {
                      window.open(dataUrl);
                    } finally {
                      URL.revokeObjectURL(dataUrl);
                    }
                  }}>
                    Inspect JSON
                  </Button>
                </div>
              </div>
              <h3 style={{
                margin: 0,
                marginBottom: "2rem",
                opacity: 0.5,
              }}>
                {date.toLocaleTimeString()} {" "} {date.toLocaleDateString()}
              </h3>
              <ExportViewer data={data} />
            </div>
          );
        } else {
          return (
            <div style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              height: "100vh",
            }}>
              <div>
                <PageSpinner message={statusMessage} errored={errored} />
              </div>
            </div>
          );
        }
      })()}
    </div>
  );
};

const ExportViewer = ({ data }: { data: ExportData }) => (
  <Twemoji options={{ className: "twemoji" }}>
    {data.messages.map((message, i) => <Message key={i} message={message} data={data} />).reverse()}
  </Twemoji>
);

const Message = ({ message, data }: { message: Message, data: ExportData }) => {
  const flags = message.flags;
  const type = message.type;

  if (type === 1) {
    return <GenericMessage message={message}>A recipient was added</GenericMessage>
  } else if (type === 2) {
    return <GenericMessage message={message}>A recipient was removed</GenericMessage>
  } else if (type === 3) {
    return <GenericMessage message={message}>A call was started</GenericMessage>
  } else if (type === 4) {
    return <GenericMessage message={message}>The channel name was changed</GenericMessage>
  } else if (type === 5) {
    return <GenericMessage message={message}>The channel icon was changed</GenericMessage>
  } else if (type === 6) {
    return <GenericMessage message={message}>A message was pinned</GenericMessage>
  } else if (type === 7) {
    return <GenericMessage message={message}>A new member joined</GenericMessage>
  } else if (type === 8) {
    return <GenericMessage message={message}>A member boosted the server</GenericMessage>
  } else if (type === 9) {
    return <GenericMessage message={message}>The server reached tier 1</GenericMessage>
  } else if (type === 10) {
    return <GenericMessage message={message}>The server reached tier 2</GenericMessage>
  } else if (type === 11) {
    return <GenericMessage message={message}>The server reached tier 3</GenericMessage>
  } else if (type === 12) {
    return <GenericMessage message={message}>A channel was followed</GenericMessage>
  } else if (type === -1) {
    return <GenericMessage message={message}>Unknown message type</GenericMessage>
  } else if (flags & 2) {
    return (
      <GenericMessage message={message} author="Crosspost">
        <DiscordMarkdown>{message.content}</DiscordMarkdown>
      </GenericMessage>
    );
  } else {
    return <NormalMessage message={message} data={data} />
  }
};

interface GenericMessageProps {
  children: ReactChild;
  message: Message;
  author?: string;
  avatarUri?: string;
}

const GenericMessage = ({ children, message, author, avatarUri }: GenericMessageProps) => {
  const date = new Date(message.timestamp);

  return (
    <div 
      className="message-content"
      style={{
        display: "flex",
        marginBottom: "0.75rem",
      }}
    >
      <img alt="" src={avatarUri || discordIcon} style ={{
        width: "32px",
        height: "32px",
        margin: "0.25rem 0.5rem 0 0",
        borderRadius: avatarUri ? "50%" : undefined,
      }} />

      <div style={{
        flexGrow: 1,
        flexShrink: 1,
        overflow: "wrap",
      }}>
        <b>{author || "Discord"}</b>

        <span style={{
          marginLeft: "0.66rem",
          opacity: 0.5,
          fontSize: "80%",
        }}>
          {date.toLocaleTimeString()} {" "} {date.toLocaleDateString()}
        </span>

        <div>{children}</div>

        {message.embeds ? (
          message.embeds.map((embed, i) => <Embed key={i} embed={embed} />)
        ) : null}

        {message.attachments ? (
          <div style={{
            marginTop: "0.25rem",
          }}>
            {message.attachments.map((attachment, i) => <Attachment key={i} attachment={attachment} />)}
          </div>
        ) : null}

        {message.reactions ? (
          <div style={{
            marginTop: "0.25rem",
          }}>
            {message.reactions.map((reaction, i) => <Reaction key={i} reaction={reaction} />)}
          </div>
        ) : null}
      </div>
    </div>
  );
};

const NormalMessage = ({ message, data }: { message: Message, data: ExportData }) => {
  const user = data.users[message.author];
  const avatarUri = user ? user.avatarUri : "https://cdn.discordapp.com/embed/avatars/0.png";
  const authorNameOverride = data.authorNames[message.author];
  const name = authorNameOverride || (user ? user.name : "Unknown member");

  return (
    <GenericMessage message={message} author={name} avatarUri={avatarUri}>
      <DiscordMarkdown>{message.content}</DiscordMarkdown>
    </GenericMessage>
  );
};

const Embed = ({ embed }: { embed: Embed }) => (
  <div style={{
    maxWidth: "520px",
    backgroundColor: BackgroundSecondaryColor,
    borderLeft: "4px solid #9b59b6",
    borderRadius: "4px",
    padding: "0.5rem 1rem 1rem 0.75rem"
  }}>
    <div>
      <b><DiscordMarkdown embed>{embed.author}</DiscordMarkdown></b>
    </div>
    <div style={{
      color: "white",
      fontSize: "105%"
    }}>
      <b><DiscordMarkdown embed>{embed.title}</DiscordMarkdown></b>
    </div>

    <div><DiscordMarkdown embed>{embed.description}</DiscordMarkdown></div>

    <div style={{
      display: "inline-grid",
    }}>
      {embed.fields.map((field, i) => <EmbedField key={i} field={field} />)}
    </div>

    <div><DiscordMarkdown embed>{embed.footer}</DiscordMarkdown></div>
  </div>
);

const EmbedField = ({ field }: { field: EmbedField }) => (
  <div style={{
    marginTop: "0.25rem",
    fontSize: "0.875rem",
  }}>
    <div>
      <b><DiscordMarkdown embed>{field.name}</DiscordMarkdown></b>
    </div>
    <div>
      <DiscordMarkdown embed>{field.value}</DiscordMarkdown>
    </div>
  </div>
);

const Attachment = ({ attachment }: { attachment: Attachment }) => {
  const content = attachment.content;

  const download = () => {
    console.log(content);
    const contentBinaryString = Base64.toUint8Array(content!);
    const contentBytes = pako.inflate(contentBinaryString);
    const contentBlob = new Blob([contentBytes]);
    fileSaver.saveAs(contentBlob, attachment.name);
  };

  return (
    <span 
      onClick={content ? download : undefined}
      style={{
        fontFamily: "monospace",
        borderRadius: "3.5px",
        padding: "0.125rem 0.25rem",
        color: content ? "white" : undefined,
        backgroundColor: content ? "#7289DA" : undefined,
        cursor: content ? "pointer" : undefined,
      }}
    >
      {attachment.name} {" "}
      {content ? <DownloadIcon /> : <BookmarkSlashIcon />}
    </span>
  );
};

const Reaction = ({ reaction }: { reaction: Reaction }) => {
  return (
    <>
      <span style={{
        fontFamily: "monospace",
        marginRight: "1rem",
      }}>
        {reaction.name} {" "} {reaction.count.toString()}
      </span>
    </>
  );
}

const DiscordMarkdown = ({ children, embed }: { children: string | null, embed?: boolean }) => {
  if (children) {
    const html = discordMarkdown.toHTML(children, { embed });
    return <span dangerouslySetInnerHTML={{ __html: html }} />;
  } else {
    return <></>;
  }
};

export default Export;
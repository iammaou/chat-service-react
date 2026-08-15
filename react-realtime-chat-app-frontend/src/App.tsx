import { useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import "./App.css";

import UserForm, { type userInfo } from "./components/UserForm";
import UserChatRoom from "./components/UserChatRoom";

export default function ChatApp() {
  const [userData, setUserData] = useState<userInfo | null>(null);
  const [serverMessage, setServerMessage] = useState<Record<
    string,
    any
  > | null>(null);

  const handleConnect = (user: userInfo) => {
    setUserData(user);
    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8088/ws"),
      reconnectDelay: 500,
      onConnect: () => {
        client.subscribe(
          `/user/${user.nickname}/queue/messages`,
          (message: IMessage) => {
            if (message.body) {
              const receivedData = JSON.parse(message.body);
              console.log("New Message:", receivedData);
              setServerMessage(receivedData);
            }
          },
        );
        client.subscribe(`/user/public`, (message: IMessage) => {
          if (message.body) {
            const receivedData = JSON.parse(message.body);
            console.log("New Message:", receivedData);
            setServerMessage(receivedData);
          }
        });

        client.publish({
          destination: `/app/user.addUser`,
          headers: {},
          body: JSON.stringify({
            nickName: user.nickname,
            fullName: user.realname,
            status: "ONLINE",
          }),
        });
      },
    });
    client.activate();
  };

  return (
    <div className="main">
      {!userData ? (
        // Passing the callback down to receive data back UP
        <UserForm onConnect={handleConnect} />
      ) : (
        // Passing the connected user info and client DOWN to ChatRoom
        <UserChatRoom userData={userData} serverMessage={serverMessage} />
      )}
    </div>
  );
}

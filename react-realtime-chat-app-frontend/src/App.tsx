import { useRef, useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import "./App.css";

import UserForm, { type userInfo } from "./components/UserForm";
import UserChatRoom from "./components/UserChatRoom";

export interface newMessageServerMessage {
  id: string;
  senderId: string;
  recipientId: string;
  content: string;
}

export interface newUserServerMessage {
  fullName: string;
  nickName: string;
  status: "ONLINE" | "OFFLINE";
}

export default function ChatApp() {
  const [userData, setUserData] = useState<userInfo | null>(null);
  const [newMessageSockJS, setnewMessageSockJS] =
    useState<newMessageServerMessage | null>(null);
  const [newUserSockJS, setnewUserSockJS] =
    useState<newUserServerMessage | null>(null);
  const clientRef = useRef<Client | null>(null);

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
              setnewMessageSockJS(receivedData);
            }
          },
        );
        client.subscribe(`/topic/public`, (message: IMessage) => {
          if (message.body) {
            const receivedData = JSON.parse(message.body);
            console.log("New Message:", receivedData);
            setnewUserSockJS(receivedData);
          }
        });

        setTimeout(() => {
          client.publish({
            destination: `/app/user.addUser`,
            headers: {},
            body: JSON.stringify({
              nickName: user.nickname,
              fullName: user.realname,
              status: "ONLINE",
            }),
          });
        }, 100);
      },
    });
    client.activate();
    clientRef.current = client;
  };

  const handleMessageSend = (
    message: string,
    sender: string | undefined,
    recipient: string | null,
  ) => {
    if (clientRef.current && clientRef.current.connected) {
      const chatMessage = {
        senderId: sender,
        recipientId: recipient,
        content: message,
        timeStamp: new Date(),
      };

      clientRef.current?.publish({
        destination: "/app/chat",
        headers: {},
        body: JSON.stringify(chatMessage),
      });
    } else {
      console.warn("WebSocket client is not connected");
    }
  };

  const handleLogout = () => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({
        destination: `/app/user.disconnectUser`,
        headers: {},
        body: JSON.stringify({
          nickName: userData?.nickname,
          fullName: userData?.realname,
          status: "OFFLINE",
        }),
      });

      window.location.reload();
    }
  };

  return (
    <div className="main">
      {!userData ? (
        // Passing the callback down to receive data back UP
        <UserForm onConnect={handleConnect} />
      ) : (
        // Passing the connected user info and client DOWN to ChatRoom
        <UserChatRoom
          userData={userData}
          newMessageSockJS={newMessageSockJS}
          newUserSockJS={newUserSockJS}
          onSendMessage={handleMessageSend}
          handleLogout={handleLogout}
        />
      )}
    </div>
  );
}

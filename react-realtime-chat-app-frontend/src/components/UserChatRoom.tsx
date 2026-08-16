import React from "react";
import "../App.css";
import type { userInfo } from "./UserForm";
import { useState } from "react";
import { useEffect } from "react";
import { useRef } from "react";

interface UserChatRoomProps {
  userData: userInfo | null;
  serverMessage: Record<string, any> | null;
  onSendMessage: (
    message: string,
    sender: string | undefined,
    recipient: string | null,
  ) => void;
  handleLogout: any;
}

interface UserInfoServer {
  nickName: string;
  fullName: string;
  status: string;
}

interface UserChatInterface {
  chatId: string;
  content: string;
  id: string;
  recipientId: string;
  senderId: string;
  timestamp: Date | null;
}

const httpAddress = "http://localhost:8088";

export const fetchConnectedUserResponse = async (
  userData: userInfo | null,
  signal?: AbortSignal,
) => {
  const response = await fetch(`${httpAddress}/users`, { signal });

  if (!response.ok) {
    throw new Error(`HTTP error! Status: ${response.status}`);
  }

  const data: UserInfoServer[] = await response.json();
  const filteredData: UserInfoServer[] = [];

  for (let i = 0; i < data.length; i++) {
    if (data[i].nickName !== userData?.nickname) {
      filteredData.push(data[i]);
    }
  }
  return filteredData;
};

export default function UserChatRoom({
  userData,
  serverMessage,
  onSendMessage,
  handleLogout,
}: UserChatRoomProps) {
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  const [userChat, setUserChat] = useState<UserChatInterface[]>([]);
  const [connectedUsers, setConnectedUsers] = useState<UserInfoServer[]>([]);
  const [notifiedUsers, setNotifiedUser] = useState<String[]>([]);
  const [userMessage, setUserMessage] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  //For fetching the whole user data from the server eighter on startup or when getting a message from the server
  useEffect(() => {
    const controller = new AbortController();

    if (!userData?.nickname) return;

    fetchConnectedUserResponse(userData, controller.signal)
      .then((data) => setConnectedUsers(data))
      .catch((err) => {
        if (err.name !== "AbortError") {
          console.error("Fetch error:", err);
        }
      });

    if (serverMessage?.senderId !== selectedUser) {
      setNotifiedUser((prev) =>
        prev.includes(serverMessage?.senderId)
          ? prev
          : [...prev, serverMessage?.senderId],
      );
    } else {
      fetchUserChat();
    }

    return () => {
      controller.abort();
    };
  }, [userData?.nickname, serverMessage]);

  // For fetching the selectefd users chat
  useEffect(() => {
    if (selectedUser !== null) {
      if (notifiedUsers.includes(selectedUser)) {
        setNotifiedUser((prev) => prev.filter((name) => name !== selectedUser));
      }

      fetchUserChat();
    }
  }, [selectedUser]);

  function handleUserClick(user: string) {
    setSelectedUser(user);
  }

  async function fetchUserChat() {
    const userChatResponse = await fetch(
      `${httpAddress}/messages/${userData?.nickname}/${selectedUser}`,
    );
    setUserChat(await userChatResponse.json());

    const timer = setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "end",
      });
    }, 50);

    console.log(userChat);

    return () => clearTimeout(timer);
  }

  const onMessageSendButtonClick = (e: any) => {
    e.preventDefault();

    console.log(userMessage, userData?.nickname, selectedUser);
    onSendMessage(userMessage, userData?.nickname, selectedUser);

    setUserMessage("");

    fetchUserChat();
  };

  return (
    <div className="chat-container" id="chat-page">
      <div className="users-list">
        <div className="users-list-container">
          <h2>Online Users</h2>
          <ul id="connectedUsers">
            {connectedUsers.map((user) => (
              <li
                className={`user-item ${selectedUser === user.nickName ? "active" : ""}`}
                key={user.nickName}
                onClick={() => handleUserClick(user.nickName)}
              >
                <img src="../../random-person.jpeg" alt="random person image" />
                {user.nickName}
                <span
                  className={
                    notifiedUsers.includes(user.nickName) ? "nbr-msg" : "hidden"
                  }
                ></span>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <p id="connected-user-fullname">{userData?.realname}</p>
          <a
            className="logout"
            href="javascript:void(0)"
            id="logout"
            onClick={handleLogout}
          >
            Logout
          </a>
        </div>
      </div>
      <div className="chat-area">
        <div className="chat-area" id="chat-messages">
          {userChat.map((message) => (
            <React.Fragment key={message.id}>
              <div
                className={`message ${message.senderId === userData?.nickname ? "sender" : "receiver"}`}
              >
                <p>{message.content}</p>
              </div>
            </React.Fragment>
          ))}
          <div
            ref={messagesEndRef}
            style={{ flexShrink: 0, height: "1px", width: "100%" }}
          />
        </div>
        <form id="messageForm" name="messageForm">
          <div className="message-input">
            <input
              autoComplete="off"
              type="text"
              id="message"
              placeholder="Type your message..."
              value={userMessage}
              onChange={(e) => setUserMessage(e.target.value)}
            />
            <button onClick={onMessageSendButtonClick}>Send</button>
          </div>
        </form>
      </div>
    </div>
  );
}

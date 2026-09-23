import React, { useLayoutEffect } from "react";
import "../App.css";
import type { userInfo } from "./UserForm";
import { useState } from "react";
import { useEffect } from "react";
import { useRef } from "react";
import type { newMessageServerMessage, newUserServerMessage } from "../App";
import type { MouseEvent } from "react";

interface UserChatRoomProps {
  userData: userInfo | null;
  newMessageSockJS: newMessageServerMessage | null;
  newUserSockJS: newUserServerMessage | null;
  onSendMessage: (
    message: string,
    sender: string | undefined,
    recipient: string | null,
  ) => void;
  handleLogout: () => void;
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
  newMessageSockJS,
  newUserSockJS,
  onSendMessage,
  handleLogout,
}: UserChatRoomProps) {
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  const [userChat, setUserChat] = useState<UserChatInterface[]>([]);
  const [connectedUsers, setConnectedUsers] = useState<UserInfoServer[]>([]);
  const [notifiedUsers, setNotifiedUser] = useState<String[]>([]);
  const [userMessage, setUserMessage] = useState("");
  const [messageCursor, setMessageCursor] = useState(null);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesStartRef = useRef<HTMLDivElement>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null); // Ref to the scrollable container
  const previousScrollHeightRef = useRef<number>(0); // To store scroll height before update
  const isReadyForMoreRef = useRef(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          isReadyForMoreRef.current && // <-- gate
          messageCursor &&
          !isLoadingMore
        ) {
          fetchUserChat("older");
        }
      },
      { threshold: 0.1 },
    );

    const currentRef = messagesStartRef.current;
    if (currentRef) observer.observe(currentRef);

    return () => {
      if (currentRef) observer.unobserve(currentRef);
    };
  }, [messageCursor, isLoadingMore, selectedUser]);

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

    const incomingSenderId = newMessageSockJS?.senderId;

    if (incomingSenderId && incomingSenderId !== selectedUser) {
      setNotifiedUser((prev) =>
        prev.includes(incomingSenderId) ? prev : [...prev, incomingSenderId],
      );
    } else {
      fetchUserChat();
    }

    return () => {
      controller.abort();
    };
  }, [userData?.nickname, newUserSockJS]);

  // For fetching the selectefd users chat
  useEffect(() => {
    if (selectedUser !== null) {
      if (notifiedUsers.includes(selectedUser)) {
        setNotifiedUser((prev) => prev.filter((name) => name !== selectedUser));
      }

      // Reset the gate — a new chat hasn't been scrolled to the bottom yet
      isReadyForMoreRef.current = false;

      fetchUserChat();
    }
  }, [selectedUser]);

  useLayoutEffect(() => {
    if (previousScrollHeightRef.current && scrollContainerRef.current) {
      const container = scrollContainerRef.current;
      const difference =
        container.scrollHeight - previousScrollHeightRef.current;
      container.scrollTop = difference;
      previousScrollHeightRef.current = 0;
    }
  }, [userChat]);

  function handleUserClick(user: string) {
    setSelectedUser(user);
  }

  async function fetchUserChat(mode: "initial" | "older" = "initial") {
    if (!selectedUser || !userData?.nickname) return;

    // Only save scroll height if we're loading older messages
    if (mode === "older" && scrollContainerRef.current) {
      previousScrollHeightRef.current = scrollContainerRef.current.scrollHeight;
    }

    setIsLoadingMore(true);

    console.log(
      `${httpAddress}/messages/${userData.nickname}/${selectedUser}?nextCursor=${messageCursor}`,
    );

    // Build URL: only include the cursor when loading older messages
    const url =
      mode === "older" && messageCursor
        ? `${httpAddress}/messages/${userData.nickname}/${selectedUser}?cursor=${messageCursor}`
        : `${httpAddress}/messages/${userData.nickname}/${selectedUser}`;

    try {
      const userChatResponse = await fetch(url);
      const res = await userChatResponse.json();

      const newMessages = res.messages.toReversed();

      if (mode === "older") {
        setUserChat((prev) => [...newMessages, ...prev]);
      } else {
        setUserChat(newMessages);

        setTimeout(() => {
          messagesEndRef.current?.scrollIntoView({
            behavior: "smooth",
            block: "end",
          });

          // The initial scroll is done, now it's safe to allow "load older"
          isReadyForMoreRef.current = true;
        }, 50);
      }

      setMessageCursor(res.nextCursor);
    } catch (err) {
      console.error("Fetch error:", err);
    } finally {
      setIsLoadingMore(false);
    }
  }

  const onMessageSendButtonClick = (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();

    if (userMessage == null || userMessage.trim() == "") {
      setUserMessage("");
      return null;
    }

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
        <div className="chat-area" id="chat-messages" ref={scrollContainerRef}>
          {userChat.map((message, index) => (
            <React.Fragment key={message.id}>
              <div
                ref={index === 0 ? messagesStartRef : null}
                className={`message ${
                  message.senderId === userData?.nickname
                    ? "sender"
                    : "receiver"
                }`}
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

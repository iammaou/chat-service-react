import { useState } from "react";

export interface userInfo {
  nickname: string;
  realname: string;
}

interface UserFormProps {
  onConnect: (userData: userInfo) => void;
}

export default function UserForm({ onConnect }: UserFormProps) {
  const [nickname, setNickname] = useState("");
  const [realname, setRealname] = useState("");

  return (
    <>
      <h2>One to One Chat | Spring boot & Websocket | By Alibou</h2>

      <div className="main_userForm">
        <h2>Enter Chatroom</h2>

        <form id="usernameForm">
          <label>Nickname:</label>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
          <label>Real name:</label>
          <input
            type="text"
            value={realname}
            onChange={(e) => setRealname(e.target.value)}
          />
          <button onClick={handleClick}>Enter Chatroom</button>
        </form>
      </div>
    </>
  );

  function handleClick(e: any) {
    e.preventDefault();
    if (nickname.trim()) {
      onConnect({ nickname, realname });
    }
  }
}

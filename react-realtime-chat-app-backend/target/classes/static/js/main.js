`use strict`;

const usernamePage = document.querySelector("#username-page");
const chatPage = document.querySelector("#chat-page");
const usernameForm = document.querySelector("#usernameForm");
const messageForm = document.querySelector("#messageForm");
const messageInput = document.querySelector("#message");
const connectingElement = document.querySelector(".connecting");
const chatArea = document.querySelector("#chat-messages");
const logout = document.querySelector("#logout");

var stompClient = null;
var nickname = null;
var fullname = null;
let selectedUserId = null;

function connect(event) {
  nickname = document.querySelector("#nickname").value.trim();
  fullname = document.querySelector("#fullname").value.trim();
  if (nickname && fullname) {
    usernamePage.classList.add("hidden");
    chatPage.classList.remove("hidden");

    // --- TRANSPORT LAYER (SockJS) ---
    // Connects to the server endpoint. If WebSockets are blocked/unsupported,
    // SockJS automatically falls back to HTTP long polling behind the scenes.
    const socket = new SockJS("/ws");

    // --- ROUTING LAYER (STOMP) ---
    // Wraps the raw SockJS tunnel so we can use structured "channels" and JSON messages
    stompClient = Stomp.over(socket);

    // Open the connection (Headers, Success Callback, Error Callback)
    stompClient.connect({}, onConnected, onError);
  }

  // Stop HTML form from refreshing the page
  event.preventDefault();
}

function onConnected() {
  // --- LISTENERS (Subscribing to Channels) ---
  // Listen to a private user-specific queue (e.g., /user/john/queue/messages)
  stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived);
  stompClient.subscribe(`/user/public`, onMessageReceived);

  // --- PUBLISHING (Sending User Status) ---
  // Tell backend we are active. Maps to backend controller destination prefix.
  stompClient.send(
    "/app/user.addUser",
    {},
    JSON.stringify({
      nickName: nickname,
      fullName: fullname,
      status: "ONLINE",
    }),
  );

  document.querySelector("#connected-user-fullname").textContent = fullname;
  console.log("connected");
  setTimeout(() => {
    findAndDisplayConnectedUsers().then();
  }, 100);
}

async function findAndDisplayConnectedUsers() {
  const connectedUserResponse = await fetch("/users");
  let connectedUsers = await connectedUserResponse.json();

  // Filter out current user and leave in all of the other ones
  connectedUsers = connectedUsers.filter((user) => user.nickName !== nickname);

  const connectedUsersList = document.querySelector("#connectedUsers");
  connectedUsersList.innerHTML = "";

  connectedUsers.forEach((user) => {
    appendUserElement(user, connectedUsersList);
    if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
      const seperator = document.createElement("li");
      seperator.classList.add("seperator");
      connectedUsersList.appendChild(seperator);
    }
  });
}

function appendUserElement(user, connectedUsersList) {
  const listItem = document.createElement("li");
  listItem.classList.add("user-item");
  listItem.id = user.nickName;

  const userImage = document.createElement("img");
  userImage.src = "../images/person.jpg";
  userImage.alt = user.fullName;

  const usernameSpan = document.createElement("span");
  usernameSpan.textContent = user.fullName;

  const receivedMsgs = document.createElement("span");
  receivedMsgs.textContent = "0";
  receivedMsgs.classList.add("nbr-msg", "hidden");

  listItem.appendChild(userImage);
  listItem.appendChild(usernameSpan);
  listItem.appendChild(receivedMsgs);

  listItem.addEventListener("click", userItemClick);

  connectedUsersList.appendChild(listItem);
}

function userItemClick(event) {
  document.querySelectorAll(".user-item").forEach((item) => {
    item.classList.remove("active");
  });
  messageForm.classList.remove("hidden");

  const clickedUser = event.currentTarget;
  clickedUser.classList.add("active");

  selectedUserId = clickedUser.getAttribute("id");
  fetchAndDisplayUserChat().then();

  const nbrMsg = clickedUser.querySelector(".nbr-msg");
  nbrMsg.classList.add("hidden");
}

async function fetchAndDisplayUserChat() {
  const userChatResponse = await fetch(
    `/messages/${nickname}/${selectedUserId}`,
  );
  const userChat = await userChatResponse.json();
  chatArea.innerHTML = "";

  userChat.forEach((chat) => {
    displayMessage(chat.senderId, chat.content);
  });

  chatArea.scrollTop = chatArea.scrollHeight;
}

function displayMessage(senderId, content) {
  const messageContainer = document.createElement("div");
  messageContainer.classList.add("message");
  if (senderId === nickname) {
    messageContainer.classList.add("sender");
  } else {
    messageContainer.classList.add("receiver");
  }
  const message = document.createElement("p");
  message.textContent = content;
  messageContainer.appendChild(message);
  chatArea.appendChild(messageContainer);
}

function onError() {}

async function onMessageReceived(payload) {
  // Refreshes the userlist
  await findAndDisplayConnectedUsers();
  const message = JSON.parse(payload.body);

  // If talking to someone AND talking to the exact person who just gave a notification than display the message in real time
  if (selectedUserId && selectedUserId === message.senderId) {
    displayMessage(message.senderId, message.content);
    chatArea.scrollTop = chatArea.scrollHeight;
  }

  // After refresh on the start of this function resetes everything to how it was otherwise
  if (selectedUserId) {
    document.querySelector(`#${selectedUserId}`).classList.add("active");
  } else {
    messageForm.classList.add("hidden");
  }

  const notifiedUser = document.querySelector(`#${message.senderId}`);
  // Grabs the user not being talked to but sending a message and showing theres a notification
  if (notifiedUser && !notifiedUser.classList.contains("active")) {
    const nbrMsg = notifiedUser.querySelector(".nbr-msg");
    nbrMsg.classList.remove("hidden");
    nbrMsg.textContent = "";
  }
}

function sendMessage(event) {
  const messageContent = messageInput.value.trim();
  if (messageContent && stompClient) {
    const chatMessage = {
      senderId: nickname,
      recipientId: selectedUserId,
      content: messageContent,
      timeStamp: new Date(),
    };
    stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
    displayMessage(nickname, chatMessage.content);

    messageInput.value = "";
  }

  chatArea.scrollTop = chatArea.scrollHeight;

  event.preventDefault();
}

function onLogout() {
  stompClient.send(
    "/app/user.disconnectUser",
    {},
    JSON.stringify({
      nickName: nickname,
      fullName: fullname,
      status: "OFFLINE",
    }),
  );

  window.location.reload();
}

usernameForm.addEventListener("submit", connect, true); // step 1
messageForm.addEventListener("submit", sendMessage, true);
logout.addEventListener("click", onLogout, true);
window.onbeforeunload = () => onLogout();

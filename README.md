# Real-Time Private Chat Application — Project Documentation

# Real-Time Private Chat Application

A real-time, bi-directional private messaging platform. Users log in with a nickname, see who's online, and chat in low-latency private threads. Built to demonstrate a hybrid HTTP + WebSocket architecture on top of Spring Boot and React.

---

## Features

- 🔐 Nickname-based login (no passwords — demo app)
- 👥 Live online users sidebar, updates in real time
- 💬 Private 1-to-1 messaging with STOMP over SockJS
- 📜 Message history with cursor-based pagination
- ⚡ Instant delivery + optimistic UI updates
- 🔔 Unread message badges per conversation
- 🗄️ MongoDB persistence with Mongo Express admin UI
- 🐳 Fully containerized — one command to run everything

---

## Architecture

The app splits its communication into two complementary paths:

- **HTTP REST (stateless)** — retrieving the online roster (`GET /users`) and loading paginated message history (`GET /messages/{senderId}/{recipientId}`).
- **WebSockets via STOMP + SockJS (stateful)** — real-time message dispatch, peer discovery, presence notifications, and unread badges.

SockJS is used as a transport fallback: if a browser or corporate firewall blocks native WebSockets, the connection gracefully downgrades to HTTP long-polling.

### Tech Stack

| Layer            | Technology                                                                |
| ---------------- | ------------------------------------------------------------------------- |
| Frontend         | React 19, TypeScript, Vite, `@stomp/stompjs`, `sockjs-client`             |
| Backend          | Java 17, Spring Boot 4.0.6, Spring WebSocket, Spring Data MongoDB, Lombok |
| Database         | MongoDB 7.0                                                               |
| DB Admin         | Mongo Express                                                             |
| Containerization | Docker, Docker Compose                                                    |
| Web Server       | Nginx (serves the frontend build)                                         |

---

## Quick Start

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Compose v2)
- That's it. No local Node, Java, or MongoDB installation is required.

### Run the stack

```bash
git clone https://github.com/iammaou/chat-service-react.git
cd chat-service-react

# Copy the environment template and adjust if needed
cp .env.example .env

# Build images and start all services
docker compose up -d --build
```

### Access the app

| Service       | URL                   | Notes                        |
| ------------- | --------------------- | ---------------------------- |
| Frontend      | http://localhost      | Main chat UI                 |
| Backend API   | http://localhost:8088 | Spring Boot REST + WebSocket |
| Mongo Express | http://localhost:8081 | DB admin UI                  |

### Try it out

1. Open http://localhost in one browser window, log in as `alice`.
2. Open http://localhost in an **incognito window** (or a different browser), log in as `bob`.
3. Both users appear in each other's sidebar. Click a name to start chatting.
4. Open DevTools → Network → WS to watch the STOMP frames flow.

---

## Stopping the App

```bash
# Stop containers, keep Mongo data
docker compose down

# Stop containers and wipe Mongo data (fresh start)
docker compose down -v

# Rebuild after code changes
docker compose up -d --build
```

---

## Configuration

All configurable values live in `.env` at the repo root. Copy `.env.example` → `.env` on first run.

| Variable               | Purpose                                   | Default                                  |
| ---------------------- | ----------------------------------------- | ---------------------------------------- |
| `MONGO_USER`           | MongoDB root username                     | `admin`                                  |
| `MONGO_PASS`           | MongoDB root password                     | `pass`                                   |
| `CORS_ALLOWED_ORIGINS` | Origins allowed to call the backend       | `http://localhost,http://localhost:5173` |
| `VITE_API_BASE_URL`    | Backend URL baked into the frontend build | `http://localhost:8088`                  |

> ⚠️ `VITE_*` values are **inlined into the frontend bundle at build time**. If you change `VITE_API_BASE_URL`, you must rebuild the frontend image:
>
> ```bash
> docker compose up -d --build frontend
> ```

---

## Local Development (without Docker)

If you want hot-reload and a faster inner loop, run services individually.

**1. Start MongoDB only:**

```bash
docker compose up -d mongo mongo-express
```

**2. Run the backend:**

```bash
cd react-realtime-chat-app-backend
./mvnw spring-boot:run
```

**3. Run the frontend:**

```bash
cd react-realtime-chat-app-frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and is already included in `CORS_ALLOWED_ORIGINS`.

---

## API Reference

### STOMP Messaging Routes

| Destination                       | Direction | Purpose                                                       |
| --------------------------------- | --------- | ------------------------------------------------------------- |
| `/app/user.addUser`               | SEND      | Called at login. Sets user status to `ONLINE`.                |
| `/app/chat`                       | SEND      | Dispatches a private chat message.                            |
| `/app/user.disconnectUser`        | SEND      | Called on logout or tab close. Sets user status to `OFFLINE`. |
| `/user/{nickname}/queue/messages` | SUBSCRIBE | Private inbound channel per user.                             |
| `/topic/public`                   | SUBSCRIBE | Broadcast channel for presence updates.                       |

### HTTP REST Endpoints

| Endpoint                                         | Method | Purpose                                 |
| ------------------------------------------------ | ------ | --------------------------------------- |
| `/users`                                         | GET    | Returns the list of online users.       |
| `/messages/{senderId}/{recipientId}`             | GET    | Returns paginated message history.      |
| `/messages/{senderId}/{recipientId}?cursor={id}` | GET    | Returns the next page (older messages). |

### WebSocket Endpoint

- **Handshake URL:** `ws://localhost:8088/ws` (SockJS)
- **Fallback transports:** xhr-streaming, xhr-polling, etc. (handled automatically by SockJS)

---

## Project Structure

```
.
├── docker-compose.yml
├── .env                       ← not committed
├── .env.example               ← committed
├── README.md
├── react-realtime-chat-app-backend/
│   ├── Dockerfile             ← multi-stage: Maven build → JRE runtime
│   ├── pom.xml
│   └── src/main/java/com/mk/websocket/
│       ├── config/            ← WebSocketConfig, CorsConfig
│       ├── user/              ← UserController, UserService, User model
│       ├── chat/              ← ChatMessage model, ChatController
│       └── WebsocketApplication.java
└── react-realtime-chat-app-frontend/
    ├── Dockerfile             ← multi-stage: Node build → Nginx serve
    ├── package.json
    └── src/
        ├── App.tsx            ← STOMP client setup, routing
        ├── components/
        │   ├── UserForm.tsx
        │   └── UserChatRoom.tsx
        └── config.ts          ← reads VITE_API_BASE_URL
```

---
# Real-Time Private Chat Application

## Goal

To provide a real-time, bi-directional private messaging platform where users can log in, view live connection statuses of online peers, and engage in low-latency chat conversations.

---

## Architecture & Tech Stack

The application isolates its workload paths using two complementary network communication patterns:
- **HTTP REST API (Stateless):** Handles non-streaming database requests, specifically retrieving the online roster (`GET /users`) and loading historical message logs (`GET /messages/{senderId}/{recipientId}`).
- **WebSockets via STOMP & SockJS (Stateful):** Handles real-time event dispatching, including direct message transmissions, dynamic peer discovery, presence notifications, and client-side unread badges.

### Project Stack Breakdown

- **Frontend:** React, SockJS-client, StompJS
- **Backend:** Java 17, Spring Boot 3.x / 4.x, Spring WebSocket, Spring Data MongoDB, Lombok
- **Database:** MongoDB
- **Database Management:** Mongo Express (Web-based administrative console)
- **Containerization:** Docker & Docker Compose

---

## How to Run

The entire application stack (Frontend, Backend, MongoDB, and Mongo Express) is fully containerized. You can build and spin up all services with a single Docker Compose command.

### Quick Start with Docker Compose

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/iammaou/chat-service-react.git
   cd websocket-chat-app
   ```

2. **Spin Up the Entire Application:**
   Run the following command to build and launch all services automatically:
   ```bash
   docker compose up -d --build
   ```

3. **Access the Application:**
   - **Frontend App (React):** http://localhost:3000
   - **Backend API (Spring Boot):** http://localhost:8088
   - **Mongo Express Admin:** http://localhost:8081

---

## Infrastructure & Environment Credentials

- **MongoDB Port:** `27017`
- **Web Admin UI Port:** `8081` (Mongo Express)
- **Default Database Username:** `admin`
- **Default Database Password:** `pass`

---

## Frontend Client API Mapping

The React frontend links to these backend routing points:

### STOMP Messaging Routes
- `/app/user.addUser` `[SEND]`: Executed at login to set user status to `ONLINE`.
- `/app/chat` `[SEND]`: Dispatches private chat payloads directly down the wire.
- `/app/user.disconnectUser` `[SEND]`: Triggers during window unloads or manual logout to declare user `OFFLINE`.
- `/user/${nickname}/queue/messages` `[SUBSCRIBE]`: Private inbound pipeline destination matching active recipient profiles.
- `/topic/public` `[SUBSCRIBE]`: Broadcast channel notifying users of active server updates.

### HTTP REST API Endpoints
- `GET /users`: Pulls list of online users to construct the sidebar interface.
- `GET /messages/${nickname}/${selectedUserId}`: Pulls chronological messaging threads from MongoDB to build the view panel layout.

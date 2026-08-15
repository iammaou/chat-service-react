# Real-Time Private Chat Application

## Goal

To provide a real-time, bi-directional private messaging platform where users can log in, view live connection statuses of online peers, and engage in low-latency chat conversations.

---

## Architecture & Communication Flow

The application isolates its workload paths using two complementary network communication patterns:

- HTTP REST API (Stateless): Handles non-streaming database requests, specifically retrieving the online roster (GET /users) and loading historical message logs (GET /messages/{senderId}/{recipientId}).
- WebSockets via STOMP & SockJS (Stateful): Handles real-time event dispatching, including direct message transmissions, dynamic peer discovery, presence notifications, and client-side unread badges.

### Project Stack Breakdown

- SockJS: Fallback connection layer protecting clients from arbitrary connectivity blocks (e.g., proxies or firewalls).
- STOMP: Framing protocol mapping inbound payloads to backend controller logic automatically.
- MongoDB: Document database optimized for storing schema-flexible messaging structures and high-frequency real-time logs.
- Mongo Express: Web-based administrative console for easy inspection of data documents in the database.

---

## Project Structure & Architecture

The Spring Boot backend organizes layers by feature domain, separating chat logs, chat rooms, and user management blocks:

src/main/java/com/mk/websocket/

  chat/ # Private Messaging Logic
    ChatController.java # STOMP /app/chat endpoints
    ChatMessage.java # Message Model Document
    ChatMessageRepository.java# MongoDB Data Layer
    ChatMessageService.java # Business Rules & Logic
    ChatNotification.java # Notification Packet DTO

  chatroom/ # Chat Session Matrix
    ChatRoom.java # Room Model Document
    ChatRoomRepository.java # MongoDB Data Layer
    ChatRoomService.java # Dynamic ID Management Logic

  config/ # Network Protocols Setup
    WebSocketConfig.java # STOMP Broker & Endpoint Mapping

  user/ # Active Presence Domain
    Status.java # Enum (ONLINE, OFFLINE)
    User.java # User Model Document
    UserController.java # HTTP REST Endpoints (/users)
    UserRepository.java # MongoDB Data Layer
    UserService.java # Status Management Engine

---

## Technical Dependencies (Maven)

Managed dependencies via pom.xml require Java 17 and Spring Boot 4.0.x:

- spring-boot-starter-websocket: Native WebSocket engine + STOMP frame protocol routing.
- spring-boot-starter-data-mongodb: Reactive/Standard MongoDB connectivity layer.
- spring-boot-starter-webmvc: Core REST infrastructure supporting standard HTTP bindings.
- lombok: Dynamic runtime compile instrumentation (Getters, Setters, Builders).

---

## Infrastructure Configuration (Docker Compose)

The developer environment is containerized locally. It exposes MongoDB and binds Mongo Express to port 8081 for administrative inspections.

### Default Environment Access Credentials

- MongoDB Port: 27017
- Web Admin UI Port: 8081 (Mongo Express)
- Default Database Username: admin
- Default Database Password: pass

---

## How to Run

### 1. Clone the Repository

git clone <repository-url>
cd websocket-chat-app

### 2. Boot Local Database Containers

Spin up your pre-configured isolated MongoDB and admin server layers in the background:
docker compose up -d

### 3. Run the Backend Application

Compile project assets and execute the Spring Boot framework target:
./mvnw spring-boot:run

### 4. Open Client Interface

Direct your local modern web browser to the hosting port to sign in:
http://localhost:8088

---

## Frontend Client API Mapping

Your JavaScript frontend links to these routing points:

### STOMP Messaging Routes

- /app/user.addUser [SEND]: Executed at login to set user status to ONLINE.
- /app/chat [SEND]: Dispatches private chat payloads directly down the wire.
- /app/user.disconnectUser [SEND]: Triggers during window unloads or manual logout to declare user OFFLINE.
- /user/${nickname}/queue/messages [SUBSCRIBE]: Private inbound pipeline destination matching active recipient profiles.
- /user/public [SUBSCRIBE]: Broad broadcast channel notifying users of active server updates.

### HTTP REST API Endpoints

- GET /users: Pulls list of online users to construct the user sidebar interface.
- GET /messages/${nickname}/${selectedUserId}: Pulls chronological messaging threads from MongoDB to build the view panel layout.

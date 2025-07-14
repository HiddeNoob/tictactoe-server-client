# XOX (Tic-Tac-Toe) Multiplayer Game

A real-time multiplayer Tic-Tac-Toe game built with Java WebSocket server and HTML5/JavaScript client, using Protocol Buffers for efficient communication.

## Features

- **Real-time multiplayer gameplay** - Play against other players in real-time
- **Lobby system** - Create and join game lobbies
- **Player identification** - Unique player IDs and usernames
- **Game state synchronization** - All players see the same game state
- **Responsive UI** - Clean, modern interface with player names display
- **WebSocket communication** - Fast, bidirectional communication
- **Protocol Buffers** - Efficient binary serialization

## Project Structure

```
xox-server/
├── client/                 # Frontend (HTML5/JavaScript)
│   ├── index.html         # Main HTML file
│   ├── script.js          # Client-side JavaScript
│   ├── style.css          # CSS styling
│   ├── game.proto         # Protocol Buffers definition (client copy)
│   └── protobuf.js        # Protocol Buffers JavaScript library
├── server/                # Backend (Java)
│   ├── src/main/
│   │   ├── java/com/game/
│   │   │   ├── connection/    # WebSocket connection handling
│   │   │   ├── model/         # Game models (Player, Lobby, Game)
│   │   │   ├── server/        # Server and connection handlers
│   │   │   └── service/       # Business logic services
│   │   └── proto/
│   │       └── game.proto     # Protocol Buffers definition
│   ├── pom.xml           # Maven configuration
│   └── README.MD         # Server-specific documentation
└── README.md             # This file
```

## Technology Stack

### Backend
- **Java 11** - Programming language
- **Maven** - Build and dependency management
- **WebSocket** - Real-time communication
- **Protocol Buffers 3.21.7** - Message serialization

### Frontend
- **HTML5** - Structure
- **CSS3** - Styling
- **JavaScript (ES6+)** - Client-side logic
- **Protocol Buffers JS** - Message deserialization
- **WebSocket API** - Server communication

## Game Flow

1. **Login**: Players enter their username and connect to the server
2. **Lobby**: Players can create new games or join existing ones
3. **Game**: Two players play Tic-Tac-Toe with real-time updates
4. **Results**: Game shows winner or draw, players can return to lobby

## Protocol Buffers Messages

The game uses the following main message types:

### Client → Server
- `InitialConnection` - Player login with username
- `CreateLobbyRequest` - Create a new game lobby
- `JoinLobbyRequest` - Join an existing lobby
- `MakeMoveRequest` - Make a move in the game

### Server → Client
- `PlayerIdResponse` - Assign unique player ID
- `LobbyListUpdate` - Update available lobbies
- `GameStateUpdate` - Update game state
- `ErrorNotification` - Error messages

## Setup and Installation

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Running the Server

1. Navigate to the server directory:
```bash
cd server
```

2. Compile and run the server:
```bash
mvn clean compile exec:java
```

The server will start on `localhost:8080`.

### Running the Client

1. Navigate to the client directory:
```bash
cd client
```

2. Serve the files using a local web server. You can use:

**Python 3:**
```bash
python -m http.server 8000
```

**Python 2:**
```bash
python -m SimpleHTTPServer 8000
```

**Node.js (if you have http-server installed):**
```bash
npx http-server -p 8000
```

**PHP:**
```bash
php -S localhost:8000
```

3. Open your browser and navigate to `http://localhost:8000`

## How to Play

1. **Connect**: Enter your username and click "Bağlan" (Connect)
2. **Create or Join**: 
   - Click "Yeni Oyun Kur" (Create New Game) to create a lobby
   - Or click "Katıl" (Join) next to an existing lobby
3. **Play**: 
   - The first player (host) gets X, the second player gets O
   - Click on empty cells to make your move
   - Player names are displayed at the top of the game screen
4. **Leave**: Click "Oyundan Ayrıl" (Leave Game) to return to the lobby

## Game Rules

- Standard Tic-Tac-Toe rules apply
- 3x3 grid
- First player to get 3 in a row (horizontal, vertical, or diagonal) wins
- If all 9 spaces are filled without a winner, it's a draw
- Players take turns, starting with X (host)

## Architecture

### Server Architecture
- **GameServer**: Main server class with WebSocket endpoint
- **ConnectionHandler**: Manages WebSocket connections and message routing
- **LobbyService**: Handles lobby creation, joining, and management
- **GameService**: Manages game logic and moves
- **NotificationService**: Sends updates to clients

### Client Architecture
- **WebSocket Connection**: Handles server communication
- **UI Management**: Dynamic view switching (login, lobby, game)
- **Game State**: Synchronizes with server state
- **Protocol Buffers**: Message encoding/decoding

## Development

### Protocol Buffers Compilation
The Maven build automatically compiles Protocol Buffers. For manual compilation:

```bash
# In server directory
mvn clean compile
```

### Debugging
- Server logs are printed to console
- Client logs are available in browser developer tools
- WebSocket traffic can be monitored in browser network tab

## Common Issues

1. **Connection Refused**: Make sure the server is running on port 8080
2. **CORS Issues**: Serve the client files through a web server, not file://
3. **Protocol Buffers Errors**: Ensure both client and server use the same .proto file


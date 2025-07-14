document.addEventListener('DOMContentLoaded', () => {
    const loginView = document.getElementById('login-view'), lobbyView = document.getElementById('lobby-view'), gameView = document.getElementById('game-view');
    const connectBtn = document.getElementById('connect-btn'), createLobbyBtn = document.getElementById('create-lobby-btn'), leaveGameBtn = document.getElementById('leave-game-btn');
    const lobbyListDiv = document.getElementById('lobby-list'), gameBoardDiv = document.getElementById('game-board'), gameStatusH2 = document.getElementById('game-status');
    const usernameInput = document.getElementById('username');

    let ws, Proto, myPlayerId;
    
    protobuf.load("game.proto").then(root => { Proto = {
        ClientMessage: root.lookupType("game.ClientMessage"), ServerMessage: root.lookupType("game.ServerMessage"),
        PlayerType: root.lookupEnum("game.PlayerType").values, Winner: root.lookupEnum("game.Winner").values,
    }});

    connectBtn.addEventListener('click', () => {
        const username = usernameInput.value.trim();
        if (!username) { return; }
        ws = new WebSocket("ws://localhost:8080");
        ws.binaryType = "arraybuffer";

        ws.onopen = () => {
            console.log("WebSocket connected");
            sendMessage({ initialConnection: { username: username } });
        };
        ws.onmessage = (event) => handleServerMessage(Proto.ServerMessage.decode(new Uint8Array(event.data)));
        ws.onclose = (event) => {
            console.log("WebSocket closed:", event.code, event.reason);
            showView('login-view');
        };
        ws.onerror = (error) => {
            console.error("WebSocket error:", error);
        };
    });
    
    const handleServerMessage = (msg) => {
        console.log("Server message:", msg);
        const type = msg.payload;
        if (type === 'error') alert(`Hata: ${msg.error.message}`);
        else if (type === 'playerId') { myPlayerId = msg.playerId.playerId; }
        else if (type === 'lobbyList') { showView('lobby-view'); updateLobbyList(msg.lobbyList.lobbies); }
        else if (type === 'gameState') { showView('game-view'); updateGameView(msg.gameState.lobby); }
    };

    const updateLobbyList = (lobbies) => {
        lobbyListDiv.innerHTML = lobbies.length === 0 ? "<p>Aktif oyun yok.</p>" : "";
        lobbies.forEach(lobby => {
            const host = lobby.players[0];
            const el = document.createElement('div');
            el.className = 'lobby';
            el.innerHTML = `<span>Kurucu: ${host.username}</span>`;
            const btn = document.createElement('button');
            btn.innerText = "Katıl";
            btn.onclick = () => sendMessage({ joinLobby: { lobbyId: lobby.id } });
            el.appendChild(btn);
            lobbyListDiv.appendChild(el);
        });
    };

    const updateGameView = (lobby) => {
        const state = lobby.gameState;
        const me = lobby.players.find(p => p.id === myPlayerId);
        if (!me) {
            console.error('Cannot find current player in lobby');
            return;
        }
        
        const hostNameDiv = document.getElementById('host-name');
        const opponentNameDiv = document.getElementById('opponent-name');
        
        const hostPlayer = lobby.players.find(p => p.type === Proto.PlayerType.X);
        const opponentPlayer = lobby.players.find(p => p.type === Proto.PlayerType.O);
        
        if (hostPlayer) {
            hostNameDiv.innerText = `${hostPlayer.username} (X)`;
            hostNameDiv.className = 'player-name host';
        }
        
        if (opponentPlayer) {
            opponentNameDiv.innerText = `${opponentPlayer.username} (O)`;
            opponentNameDiv.className = 'player-name opponent';
        } else {
            opponentNameDiv.innerText = 'Rakip bekleniyor...';
            opponentNameDiv.className = 'player-name opponent';
        }
        
        const typeSymbols = { [Proto.PlayerType.X]: 'X', [Proto.PlayerType.O]: 'O' };
        gameBoardDiv.innerHTML = "";
        state.board.forEach((cell, i) => {
            const el = document.createElement('div');
            el.className = `cell ${typeSymbols[cell] || ''}`;
            el.innerText = typeSymbols[cell] || "";
            if (cell === Proto.PlayerType.TYPE_UNSPECIFIED && state.winner === Proto.Winner.WINNER_UNSPECIFIED && state.currentTurn === me.type) {
                el.onclick = () => sendMessage({ makeMove: { position: i } });
            }
            gameBoardDiv.appendChild(el);
        });

        if (state.winner !== Proto.Winner.WINNER_UNSPECIFIED) {
            const winnerText = { [Proto.Winner.PLAYER_X]: "Kazanan: X", [Proto.Winner.PLAYER_O]: "Kazanan: O", [Proto.Winner.DRAW]: "Berabere!" };
            gameStatusH2.innerText = winnerText[state.winner];
        } else {
            const turnText = state.currentTurn === me.type ? "Sıra sizde" : "Rakip bekleniyor";
            gameStatusH2.innerText = `${turnText} (${typeSymbols[me.type]})`;
        }
    };
    
    const sendMessage = (payload) => ws.send(Proto.ClientMessage.encode(Proto.ClientMessage.create(payload)).finish());
    createLobbyBtn.addEventListener('click', () => sendMessage({ createLobby: {} }));
    leaveGameBtn.addEventListener('click', () => {
        showView('login-view');
        ws.close();
    });

    const showView = (viewId) => {
        [loginView, lobbyView, gameView].forEach(v => v.classList.add('hidden'));
        document.getElementById(viewId).classList.remove('hidden');
    };
});
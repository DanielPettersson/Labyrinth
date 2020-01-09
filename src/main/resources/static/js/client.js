class GameClient {
    constructor(onConnect, onError, onGameJoined, onGameStarted, onGameEnded, onGameState) {
        this.socket = new WebSocket('ws://' + location.host + '/ws');

        this.socket.addEventListener('open', function (event) {
            onConnect.call(this, event);
        });

        this.socket.addEventListener('error', function (event) {
            onError.call(this, event);
        });

        this.socket.addEventListener('message', function (event) {

            let data = JSON.parse(event.data);

            switch (data.command) {
                case 'joined':
                    onGameJoined.call(this, data.content);
                    break;
                case 'started':
                    onGameStarted.call(this, data.content);
                    break;
                case 'ended':
                    onGameEnded.call(this, data.content);
                    break;
                case 'state':
                    onGameState.call(this, data.content);
                    break;
                case 'error':
                    onError.call(this, data.content);
                    break;

            }

        });
    }

    join(numPlayers, gameSize) {
        this.command('join', { numPlayers: numPlayers, gameSize: gameSize });
    }

    move(gameId, to) {
        this.command('move', { gameId: gameId, to: to });
    }

    command(command, content) {
        this.socket.send(JSON.stringify({
            command: command,
            content: content
        }));
    }

}
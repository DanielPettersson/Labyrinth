class Main {

    constructor() {

        let gameClient;

        let joinTimeout;
        let joinButton = document.getElementById('joinButton');
        let gameArea = document.getElementById('gameArea');
        let numPlayersSelect = document.getElementById('numPlayers');
        let gameSizeInput = document.getElementById('gameSize');
        let formDiv = document.getElementById('formDiv');

        let playerIndex;
        let game;

        gameClient = new GameClient(
            function (ev) {
                joinButton.removeAttribute('disabled');
            },
            function (error) {
                formDiv.style.display = 'none';
                gameArea.innerText = error;
            },
            function (info) {

                playerIndex = info.playerIndex;
                if (info.game) {

                    gameArea.innerText = '';
                    game = new Game(gameClient, gameArea, info.game, info.playerIndex);
                } else {
                    joinTimeout = setTimeout(function () {
                        window.location.reload();
                    }, 30000);
                }
            },
            function (gameData) {

                clearTimeout(joinTimeout);

                gameArea.innerText = '';
                game = new Game(gameClient, gameArea, gameData, playerIndex);
            },
            function (gameEnded) {
                game.endGame(gameEnded);
            },
            function (gameState) {
                game.setGameState(gameState);
            }
        );

        joinButton.onclick = function(ev) {
            setTimeout(function() {

                formDiv.style.display = 'none';
                gameArea.innerText = 'Waiting for other players...';
                if (USER_IS_TOUCH) gameArea.requestFullscreen();

                var numPlayers = numPlayersSelect.options[numPlayersSelect.selectedIndex].value;
                var gameSize = gameSizeInput.value;
                gameClient.join(numPlayers, gameSize)

            }, 200);
        };


    }
}

// Check if user is on a touch device

USER_IS_TOUCH = false;
window.addEventListener('touchstart', function onFirstTouch() {
    USER_IS_TOUCH = true;
    window.removeEventListener('touchstart', onFirstTouch, false);
});

// Fullscreen polyfill

if (!Element.prototype.requestFullscreen) {
    Element.prototype.requestFullscreen = Element.prototype.mozRequestFullscreen || Element.prototype.webkitRequestFullscreen || Element.prototype.msRequestFullscreen;
}

// Initialize game

new Main();


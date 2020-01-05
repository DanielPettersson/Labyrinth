var gameClient;
var joinInfo;
var onGameState;
var onGameEnded;
var joinTimeout;
var joinButton = document.getElementById('joinButton');
var gameArea = document.getElementById('gameArea');
var numPlayersSelect = document.getElementById('numPlayers');
var gameSizeInput = document.getElementById('gameSize');
var formDiv = document.getElementById('formDiv');

function connect() {

    gameClient = new GameClient(
        function (ev) {
            joinButton.removeAttribute('disabled');
        },
        function (ev) {
            formDiv.style.display = 'none';
            gameArea.innerText = ev.data;
        },
        function (info) {
            joinInfo = info;
            if (joinInfo.game) {
                var gs = gameStart(joinInfo.game, joinInfo, gameClient);
                onGameState = gs.onGameState;
                onGameEnded = gs.onGameEnded;
            } else {
                joinTimeout = setTimeout(function () {
                    window.location.reload();
                }, 5000);
            }
        },
        function (game) {
            clearTimeout(joinTimeout);
            var gs = gameStart(game, joinInfo, gameClient);
            onGameState = gs.onGameState;
            onGameEnded = gs.onGameEnded;

        },
        function(aborted) {
            formDiv.style.display = 'block';
            gameArea.innerText = '';
        },
        function (gameEnded) {
            onGameEnded.call(this, gameEnded);
        },
        function (gameState) {
            onGameState.call(this, gameState);
        }

    );

}

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

connect();


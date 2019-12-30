var stompClient;
var joinButton = document.getElementById('joinButton');
var gameArea = document.getElementById('gameArea');
var numPlayersSelect = document.getElementById('numPlayers');
var gameSizeInput = document.getElementById('gameSize');
var formDiv = document.getElementById('formDiv');

function connect() {

    var socket = new SockJS('/game');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        joinButton.removeAttribute('disabled');

        stompClient.subscribe('/user/topic/error', function (message) {
            formDiv.style.display = 'none';
            gameArea.innerText = message.body;
        });
    });
}

joinButton.onclick = function(ev) {
    setTimeout(function() {

        formDiv.style.display = 'none';
        gameArea.innerText = 'Waiting for other players...';
        if (USER_IS_TOUCH) gameArea.requestFullscreen();

        var gameRequestJoinedSubscription = stompClient.subscribe('/user/topic/game-joined', function (message) {
            gameRequestJoinedSubscription.unsubscribe();
            var joinInfo = JSON.parse(message.body);

            if (joinInfo.game) {
                gameStart(joinInfo.game, joinInfo);
            } else {

                var gameAbortedSubscription = stompClient.subscribe('/topic/game-request-aborted/' + joinInfo.gameUuid, function (message) {
                    gameAbortedSubscription.unsubscribe();
                    gameStartedSubscription.unsubscribe();
                    formDiv.style.display = 'block';
                    gameArea.innerText = '';
                });

                var gameStartedSubscription = stompClient.subscribe('/topic/game-started/' + joinInfo.gameUuid, function (message) {
                    gameAbortedSubscription.unsubscribe();
                    gameStartedSubscription.unsubscribe();
                    gameStart(JSON.parse(message.body), joinInfo);
                });
            }

        });

        var numPlayers = numPlayersSelect.options[numPlayersSelect.selectedIndex].value;
        var gameSize = gameSizeInput.value;
        stompClient.send('/app/join-game', {}, JSON.stringify({ numPlayers: numPlayers, gameSize: gameSize }));

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
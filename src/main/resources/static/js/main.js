var stompClient;
var createButton = document.getElementById('createButton');
var joinButton = document.getElementById('joinButton');
var gameRequests = document.getElementById('gameRequests');
var gameArea = document.getElementById('gameArea');

function connect() {

    var socket = new SockJS('/game');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {

        createButton.removeAttribute('disabled');
        joinButton.removeAttribute('disabled');

    });
}

function gameStart(gameId) {
    gameArea.innerText = 'Game on ' + gameId;
}

createButton.onclick = function(ev) {

    createButton.style.display = 'none';
    joinButton.style.display = 'none';

    stompClient.subscribe('/user/topic/game-request-created', function (message) {
        var uuid = JSON.parse(message.body).uuid;
        gameArea.innerText = 'Wating for game to start...';

        stompClient.send('/app/game-request-added', {}, {});
        stompClient.subscribe('/topic/game-started/' + uuid, function (m) {
            gameStart(uuid);
        });
        stompClient.subscribe('/topic/game-aborted/' + uuid, function (m) {
            gameArea.innerText = '';
            createButton.style.display = 'inline';
            joinButton.style.display = 'inline';
        });
    });
    stompClient.send('/app/create-game-request', {}, {});

};


gameRequests.onclick = function (ev) {
    gameRequests.style.display = 'none';
    var gameId = ev.target.innerText;
    stompClient.subscribe('/topic/game-started/' + gameId, function (message) {
        gameStart(gameId);
    });
    stompClient.send('/app/start-game/' + gameId, {}, {});
};

joinButton.onclick = function(ev) {

    createButton.style.display = 'none';
    joinButton.style.display = 'none';
    gameRequests.style.display = 'block';

    var handleGameRequests = function (message) {
        gameRequests.innerHTML = '';

        var requests = JSON.parse(message.body);
        requests.forEach(function(r) {
            var li = document.createElement('li');
            li.innerText = r.uuid;
            gameRequests.appendChild(li);
        });

    };

    stompClient.subscribe('/user/topic/game-requests', handleGameRequests);
    stompClient.subscribe('/topic/game-requests', handleGameRequests);
    stompClient.send('/app/get-game-requests', {}, {});

};

connect();
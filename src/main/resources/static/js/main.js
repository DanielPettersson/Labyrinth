var stompClient;
var createButton = document.getElementById('createButton');
var joinButton = document.getElementById('joinButton');
var gameRequests = document.getElementById('gameRequests');
var gameArea = document.getElementById('gameArea');

var userGameRequestsSubscription;
var gameRequestsSubscription;

function connect() {

    var socket = new SockJS('/game');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {

        createButton.removeAttribute('disabled');
        joinButton.removeAttribute('disabled');

    });
}

createButton.onclick = function(ev) {

    createButton.style.display = 'none';
    joinButton.style.display = 'none';

    var gameRequestCreatedSubscription = stompClient.subscribe('/user/topic/game-request-created', function (message) {
        gameRequestCreatedSubscription.unsubscribe();
        var uuid = JSON.parse(message.body).uuid;
        gameArea.innerText = 'Wating for game to start...';

        var gameStartedSubscription = stompClient.subscribe('/topic/game-started/' + uuid, function (m) {
            gameStartedSubscription.unsubscribe();
            gameStart(JSON.parse(m.body));
        });
        var gameAbortedSubscription = stompClient.subscribe('/topic/game-aborted/' + uuid, function (m) {
            gameStartedSubscription.unsubscribe();
            gameArea.innerText = '';
            createButton.style.display = 'inline';
            joinButton.style.display = 'inline';
        });
    });
    stompClient.send('/app/create-game-request', {}, {});

};

gameRequests.onclick = function (ev) {
    userGameRequestsSubscription.unsubscribe();
    gameRequestsSubscription.unsubscribe();

    gameRequests.style.display = 'none';
    var gameId = ev.target.innerText;
    var gameStartedSubscription = stompClient.subscribe('/topic/game-started/' + gameId, function (message) {
        gameStartedSubscription.unsubscribe();
        gameStart(JSON.parse(message.body));
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

    userGameRequestsSubscription = stompClient.subscribe('/user/topic/game-requests', handleGameRequests);
    gameRequestsSubscription = stompClient.subscribe('/topic/game-requests', handleGameRequests);
    stompClient.send('/app/get-game-requests', {}, {});

};

connect();
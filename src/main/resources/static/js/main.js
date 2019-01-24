function connect() {
    var socket = new SockJS('/game');
    var stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/newgame', function (message) {
            var messageElement = document.createElement('div');
            messageElement.innerText = message.body;
            document.body.appendChild(messageElement);
        });
        stompClient.send('/app/creategame', {}, {});
    });
}

connect();
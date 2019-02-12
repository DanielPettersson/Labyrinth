function gameStart(game, playerUuid) {

    gameArea.innerText = '';

    var WIDTH = window.innerWidth;
    var HEIGHT = window.innerHeight;
    var windowHalfX = WIDTH / 2;
    var windowHalfY = HEIGHT / 2;
    var mouseX = 0, mouseY = 0;

    document.addEventListener( 'mousemove', onDocumentMouseMove, false );
    document.addEventListener( 'mouseleave', onDocumentMouseLeave, false );
    window.addEventListener( 'resize', onWindowResize, false );

    var labyrinthSize = game.labyrinth.size;
    var halfLabyrinthSize = labyrinthSize / 2;

    var scene = new THREE.Scene();
    var camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 0.1, 1000 );
    camera.position.z = labyrinthSize * 1.2;

    var renderer = new THREE.WebGLRenderer();
    renderer.setSize( window.innerWidth, window.innerHeight );
    document.body.appendChild( renderer.domElement );

    var light1 = new THREE.PointLight( 0xffffff, 1, 100 );
    light1.position.set( labyrinthSize, labyrinthSize, labyrinthSize );
    scene.add( light1 );

    var light2 = new THREE.PointLight( 0xffffff, 1, 100 );
    light2.position.set( labyrinthSize*0.5, -labyrinthSize*0.5, labyrinthSize );
    scene.add( light2 );

    createLabyrinthModel();

    var player1 = createPlayer(game.players[0]);
    var player2 = createPlayer(game.players[1]);
    positionPlayers(game.players);

    stompClient.subscribe('/topic/player-moved/' + game.uuid, function (message) {
        positionPlayers(JSON.parse(message.body));
    });

    function doMove(move) {
        if (move) {
            stompClient.send('/app/move-player/' + game.uuid + '/' + playerUuid, {}, JSON.stringify(move));
        }
    }

    handleInput();

    function animate() {
        requestAnimationFrame( animate );

        camera.position.x += ( - mouseX - camera.position.x ) * .05;
        camera.position.y += ( - mouseY - camera.position.y ) * .05;
        camera.lookAt( scene.position );

        renderer.render( scene, camera );
    }
    animate();

    function createLabyrinthModel() {

        var labyrinthPlaneMaterial = new THREE.MeshLambertMaterial( { color: 0x006666 } );
        var labyrinthWallMaterial = new THREE.MeshLambertMaterial( { color: 0xFFFF00 } );

        var labyrinthPlane = new THREE.Mesh( new THREE.PlaneBufferGeometry(labyrinthSize, labyrinthSize), labyrinthPlaneMaterial);
        labyrinthPlane.receiveShadow = true;
        scene.add( labyrinthPlane );

        var northLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( labyrinthSize, 0.1, 0.5 ), labyrinthWallMaterial );
        northLabyrinthWall.position.set(0, -halfLabyrinthSize, 0.24);
        scene.add( northLabyrinthWall );

        var eastLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, labyrinthSize, 0.5 ), labyrinthWallMaterial );
        eastLabyrinthWall.position.set(halfLabyrinthSize, 0, 0.24);
        scene.add( eastLabyrinthWall );

        var southLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( labyrinthSize, 0.1, 0.5 ), labyrinthWallMaterial );
        southLabyrinthWall.position.set(0, halfLabyrinthSize, 0.24);
        scene.add( southLabyrinthWall );

        var westLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, labyrinthSize, 0.5 ), labyrinthWallMaterial );
        westLabyrinthWall.position.set(-halfLabyrinthSize, 0, 0.24);
        scene.add( westLabyrinthWall );

        for (var y = 0; y < labyrinthSize; y++) {
            for (var x = 0; x < labyrinthSize; x++) {
                var cell = game.labyrinth.cells[y][x];

                if (cell.walls[0] && y !== 0) {
                    var northCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 1, 0.1, 0.5 ), labyrinthWallMaterial );
                    northCellWall.position.set(-halfLabyrinthSize + x + 0.5, -halfLabyrinthSize + y, 0.24);
                    scene.add( northCellWall );
                }

                if (cell.walls[3] && x !== 0) {
                    var westCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, 1, 0.5 ), labyrinthWallMaterial );
                    westCellWall.position.set(-halfLabyrinthSize + x, -halfLabyrinthSize + y + 0.5, 0.24);
                    scene.add( westCellWall );
                }

            }
        }
    }

    function createPlayer(playerData) {
        var player = new THREE.Mesh( new THREE.SphereBufferGeometry(0.4, 20, 20), new THREE.MeshLambertMaterial( { color: 0xFF0000 } ) );
        scene.add(player);
        return player;
    }

    function positionPlayers(playersData) {
        positionPlayer(player1, playersData[0]);
        positionPlayer(player2, playersData[1]);
    }

    function positionPlayer(player, playerData) {
        player.position.set(-halfLabyrinthSize + playerData.location.x + 0.5, -halfLabyrinthSize + playerData.location.y + 0.5, 0.2);
    }

    function onDocumentMouseMove( event ) {
        mouseX = ( event.clientX - windowHalfX ) / 200.0;
        mouseY = ( event.clientY - windowHalfY ) / 200.0;
    }

    function onDocumentMouseLeave( event ) {
        mouseX = 0;
        mouseY = 0;
    }

    function onWindowResize() {
        WIDTH = window.innerWidth;
        HEIGHT = window.innerHeight;
        windowHalfX = WIDTH / 2;
        windowHalfY = HEIGHT / 2;
        renderer.setSize( WIDTH, HEIGHT );
        camera.aspect = WIDTH / HEIGHT;
        camera.updateProjectionMatrix();
    }

    function handleInput() {
        document.addEventListener('keyup', function(keyEvent) {

            var move;

            switch (keyEvent.key) {
                case 'ArrowUp':
                    move = { x: 0, y: 1};
                    break;
                case 'ArrowDown':
                    move = { x: 0, y: -1};
                    break;
                case 'ArrowLeft':
                    move = { x: -1, y: 0};
                    break;
                case 'ArrowRight':
                    move = { x: 1, y: 0};
            }
            doMove(move);

        });

        document.addEventListener('touchstart', handleTouchStart, false);
        document.addEventListener('touchmove', handleTouchMove, false);

        var xDown = null;
        var yDown = null;

        function getTouches(evt) {
            return evt.touches ||             // browser API
                evt.originalEvent.touches; // jQuery
        }

        function handleTouchStart(evt) {
            var firstTouch = getTouches(evt)[0];
            xDown = firstTouch.clientX;
            yDown = firstTouch.clientY;
        }

        function handleTouchMove(evt) {
            if ( ! xDown || ! yDown ) {
                return;
            }

            var xUp = evt.touches[0].clientX;
            var yUp = evt.touches[0].clientY;

            var xDiff = xDown - xUp;
            var yDiff = yDown - yUp;

            if ( Math.abs( xDiff ) > Math.abs( yDiff ) ) {
                if ( xDiff > 0 ) {
                    doMove({ x: -1, y: 0});
                } else {
                    doMove({ x: 1, y: 0});
                }
            } else {
                if ( yDiff > 0 ) {
                    doMove({ x: 0, y: 1});
                } else {
                    doMove({ x: 0, y: -1});
                }
            }

            xDown = null;
            yDown = null;
        }
    }

}


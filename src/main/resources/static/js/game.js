class Game {

    constructor(gameClient, gameElement, gameData, playerIndex) {

        // Initialize members

        this.gameClient = gameClient;

        this.colors = [0xff0000, 0x0000ff, 0x00ff00, 0xff00ff];
        let _colors = this.colors;

        this.canMove = true;

        this.gameId = gameData.uuid;

        this.labyrinthSize = gameData["labyrinth"].size;
        let _labyrinthSize = this.labyrinthSize;

        this.halfLabyrinthSize = this.labyrinthSize / 2;
        let _halfLabyrinthSize =  this.halfLabyrinthSize;

        this.scene = new THREE.Scene();
        let _scene = this.scene;

        this.camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 0.1, 1000 );
        let zoomOutFactor = Math.max(1, (window.innerHeight / window.innerWidth) * 0.8);
        this.camera.position.z = this.labyrinthSize * zoomOutFactor;
        let _camera = this.camera;

        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        this.renderer.setClearColor(this.colors[playerIndex], 1);
        let _renderer = this.renderer;

        gameElement.appendChild( this.renderer.domElement );

        // Setup lights

        var light1 = new THREE.PointLight( 0xffffff, 1, 100 );
        light1.position.set( this.labyrinthSize, this.labyrinthSize, this.labyrinthSize );
        this.scene.add( light1 );

        var light2 = new THREE.PointLight( 0xffffff, 1, 100 );
        light2.position.set( this.labyrinthSize*0.5, -this.labyrinthSize*0.5, this.labyrinthSize );
        this.scene.add( light2 );

        // Setup players

        this.players = createPlayers(gameData.players);
        this._positionPlayers(this.players, gameData.players);
        let _players = this.players;

        function createPlayers(playersData) {
            var players = [];
            for (var i in playersData) players.push(createPlayer(playersData[i], i));
            return players;
        }

        function createPlayer(playerData, playerIndex) {
            var player = new THREE.Mesh( new THREE.SphereBufferGeometry(0.4, 20, 20), new THREE.MeshLambertMaterial( { color: _colors[playerIndex] } ) );
            player.targetPosition = new THREE.Vector3();
            player.position.set(0, 0, _labyrinthSize);
            _scene.add(player);
            return player;
        }

        // Setup labyrinth model

        this.labyrinthCells = createLabyrinthModel();
        let _labyrinthCells = this.labyrinthCells;

        function createLabyrinthModel() {

            var labyrinthWallMaterial = new THREE.MeshLambertMaterial( { color: 0xFFFF00 } );

            var labyrinthPlane = new THREE.Mesh( new THREE.PlaneBufferGeometry(_labyrinthSize, _labyrinthSize), new THREE.MeshLambertMaterial( { color: 0x999999 } ));
            _scene.add(labyrinthPlane);

            var northLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( _labyrinthSize, 0.1, 0.5 ), labyrinthWallMaterial );
            northLabyrinthWall.position.set(0, -_halfLabyrinthSize, 0.24);
            _scene.add( northLabyrinthWall );

            var eastLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, _labyrinthSize, 0.5 ), labyrinthWallMaterial );
            eastLabyrinthWall.position.set(_halfLabyrinthSize, 0, 0.24);
            _scene.add( eastLabyrinthWall );

            var southLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( _labyrinthSize, 0.1, 0.5 ), labyrinthWallMaterial );
            southLabyrinthWall.position.set(0, _halfLabyrinthSize, 0.24);
            _scene.add( southLabyrinthWall );

            var westLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, _labyrinthSize, 0.5 ), labyrinthWallMaterial );
            westLabyrinthWall.position.set(-_halfLabyrinthSize, 0, 0.24);
            _scene.add( westLabyrinthWall );

            var labyrinthCells = [];

            for (var y = 0; y < _labyrinthSize; y++) {

                labyrinthCells.push([]);

                for (var x = 0; x < _labyrinthSize; x++) {

                    var ownerMarker = new THREE.Mesh( new THREE.CircleGeometry(0.3, 10), new THREE.MeshLambertMaterial( { color: 0x999999 } ));
                    ownerMarker.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y + 0.5, 0.01);
                    ownerMarker.targetColor = new THREE.Color(0x999999);
                    _scene.add( ownerMarker );

                    var visitableMarker = new THREE.Mesh( new THREE.BoxBufferGeometry(0.9, 0.9, 0.6), new THREE.MeshLambertMaterial( { color: 0x777777, transparent: true, opacity: 0.0}  ))
                    visitableMarker.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y + 0.5, -0.3);
                    visitableMarker.targetOpacity = 0.0;
                    visitableMarker.targetPositionZ = -0.3;
                    _scene.add( visitableMarker );

                    labyrinthCells[y].push({
                        ownerMarker: ownerMarker,
                        visitableMarker: visitableMarker
                    });

                    var cell = gameData.labyrinth.cells[y][x];

                    if (cell.walls[0] && y !== 0) {
                        var northCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 1, 0.1, 0.5 ), labyrinthWallMaterial );
                        northCellWall.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y, 0.24);
                        _scene.add( northCellWall );
                    }

                    if (cell.walls[3] && x !== 0) {
                        var westCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, 1, 0.5 ), labyrinthWallMaterial );
                        westCellWall.position.set(-_halfLabyrinthSize + x, -_halfLabyrinthSize + y + 0.5, 0.24);
                        _scene.add( westCellWall );
                    }

                }
            }

            return labyrinthCells;
        }

        // Start game loop

        function animate() {
            requestAnimationFrame( animate );

            _camera.lookAt( _scene.position );

            _players.forEach(function(player) {
                var move = player.targetPosition.clone().sub(player.position).multiplyScalar(0.12);
                player.position.copy(player.position.add(move));
            });

            for (var y = 0; y < _labyrinthSize; y++) {
                for (var x = 0; x < _labyrinthSize; x++) {
                    var lc = _labyrinthCells[y][x];
                    lc.ownerMarker.material.color.lerp(lc.ownerMarker.targetColor, 0.01);
                    lc.visitableMarker.material.opacity += (lc.visitableMarker.targetOpacity - lc.visitableMarker.material.opacity) * 0.03;
                    lc.visitableMarker.position.z += (lc.visitableMarker.targetPositionZ - lc.visitableMarker.position.z) * 0.03;
                }
            }

            _renderer.render( _scene, _camera );
        }

        animate();

        // Setup input handler

        new InputHandler(this);

    }



    doMove(move) {
        if (move && this.canMove) {
            this.gameClient.move(this.gameId, move);
        }
    }

    setGameState(gameState) {
        this._positionPlayers(this.players, gameState.players);

        for (var y = 0; y < this.labyrinthSize; y++) {
            for (var x = 0; x < this.labyrinthSize; x++) {
                this.labyrinthCells[y][x].ownerMarker.targetColor.set(this.colors[gameState.cellsOwnerIndices[y][x]]);
                this.labyrinthCells[y][x].visitableMarker.targetOpacity = gameState.cellsVisitable[y][x] ? 0.0 : 0.7;
                this.labyrinthCells[y][x].visitableMarker.targetPositionZ = gameState.cellsVisitable[y][x] ? -0.3 : 0.3;
            }
        }
    }

    endGame(gameEnded) {
        this.canMove = false;

        let _halfLabyrinthSize = this.halfLabyrinthSize;
        let _scene = this.scene;

        new THREE.FontLoader().load( 'fonts/helvetiker_regular.typeface.json', function ( font ) {

            for (var i = 0; i < gameEnded.players.length; i++) {

                var playerPoints = gameEnded.points[i];
                var playerLocation = gameEnded.players[i].location;

                var pointsGeo = new THREE.TextBufferGeometry('' + playerPoints, { font: font, size: 0.5, height: 0.2 });
                pointsGeo.computeBoundingBox();
                var textWidth = pointsGeo.boundingBox.max.x - pointsGeo.boundingBox.min.x;
                var pointsMesh = new THREE.Mesh(pointsGeo, new THREE.MeshLambertMaterial( { color: 0xeeeeee } ));
                pointsMesh.position.set(-_halfLabyrinthSize + playerLocation.x + 0.5 - textWidth * 0.5, -_halfLabyrinthSize + playerLocation.y + 0.2, 0.6);
                _scene.add(pointsMesh);

            }
        });
    };

    resize(width, height) {
        this.renderer.setSize( width, height );
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
    }

    _positionPlayers(players, playersData) {
        for (var i in players) this._positionPlayer(players[i], playersData[i]);
    }

    _positionPlayer(player, playerData) {
        player.targetPosition.set(-this.halfLabyrinthSize + playerData.location.x + 0.5, -this.halfLabyrinthSize + playerData.location.y + 0.5, 0.2);
    }

}

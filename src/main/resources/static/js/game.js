/* global THREE */

class Game {

    constructor(gameClient, gameElement, gameData, playerIndex) {
        
        const mainLightMaxPower = 2.0;
        const mainLightMinPower = 0.5;
        
        let stats = new Stats();
        stats.showPanel( 1 );
        document.body.appendChild( stats.dom );

        // Initialize members

        this.gameClient = gameClient;

        this.colors = [0xff3333, 0x3333ff, 0x33ff33, 0xff33ff];
        let _colors = this.colors;

        this.canMove = true;

        this.gameId = gameData.uuid;
        this.playerIndex = playerIndex;

        this.labyrinthSize = gameData.labyrinth.size;
        let _labyrinthSize = this.labyrinthSize;

        this.halfLabyrinthSize = this.labyrinthSize / 2;
        let _halfLabyrinthSize =  this.halfLabyrinthSize;

        this.scene = new THREE.Scene();
        let _scene = this.scene;

        this.camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 0.1, 1000 );
        let zoomOutFactor = Math.max(1, (window.innerHeight / window.innerWidth) * 0.8);
        this.camera.position.z = this.labyrinthSize * zoomOutFactor;
        this.camera.lookAt(this.scene.position);
        let _camera = this.camera;

        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.shadowMap.enabled = true;
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        let _renderer = this.renderer;

        gameElement.appendChild( this.renderer.domElement );
        
        // Setup lights

        this.mainLight1 = new THREE.SpotLight( 0xffffff);
        this.mainLight1.position.set(_halfLabyrinthSize, _halfLabyrinthSize, _labyrinthSize);
        this.mainLight1.castShadow = true;
        this.mainLight1.power = mainLightMaxPower;
        this.scene.add( this.mainLight1 );
        let _mainLight1 = this.mainLight1;
        
        this.mainLight2 = new THREE.SpotLight( 0xffffff);
        this.mainLight2.position.set(-_halfLabyrinthSize, -_halfLabyrinthSize, _labyrinthSize);
        this.mainLight2.castShadow = true;
        this.mainLight2.power = mainLightMaxPower;
        this.scene.add( this.mainLight2 );
        let _mainLight2 = this.mainLight2;
        
        // Setup players

        this.players = createPlayers(gameData.players);
        this._positionPlayers(this.players, gameData.players);
        let _players = this.players;

        function createPlayers(playersData) {
            var players = [];
            for (var i in playersData) players.push(createPlayer(playersData[i], parseInt(i)));
            return players;
        }

        function createPlayer(playerData, playerIndex) {
            
            var group = new THREE.Group();
            group.targetPosition = new THREE.Vector3();
            group.position.set(0, 0, _labyrinthSize);
            
            var player = new THREE.Mesh( new THREE.TorusKnotBufferGeometry(0.20, 0.05), new THREE.MeshStandardMaterial( { color: _colors[playerIndex] } ) );
            player.receiveShadow = true;
            player.castShadow = true;            
            group.add(player);
            
            var playerLight = new THREE.PointLight( _colors[playerIndex], 1.3, 5, 2 );
            playerLight.position.set(0, 0, 0.3);
            playerLight.castShadow = true;
            playerLight.shadow.mapSize.width = 256;
            playerLight.shadow.mapSize.height = 256;
            playerLight.shadow.camera.near = 0.1;
            playerLight.shadow.camera.far = 5;
            _scene.add( playerLight );
            group.add(playerLight);
            
            _scene.add(group);
            
            return group;
        }

        // Setup labyrinth model

        this.labyrinthCells = createLabyrinthModel();
        let _labyrinthCells = this.labyrinthCells;

        function createLabyrinthModel() {


            let loader = new THREE.TextureLoader();
            let wallTexture = loader.load('img/wall.jpg');
            let floorTexture = loader.load('img/floor.jpg');
            floorTexture.wrapS = THREE.RepeatWrapping;
            floorTexture.wrapT = THREE.RepeatWrapping;
            floorTexture.repeat.set(_halfLabyrinthSize, _halfLabyrinthSize);
            let ownerMarkerFloorTexture = loader.load('img/floor.jpg');
            ownerMarkerFloorTexture.repeat.set(0.25, 0.25);
            
            var labyrinthWallMaterial = new THREE.MeshStandardMaterial( { map: wallTexture, metalnessMap: wallTexture } );

            var labyrinthPlane = new THREE.Mesh( 
                    new THREE.PlaneBufferGeometry(_labyrinthSize, _labyrinthSize), 
                    new THREE.MeshStandardMaterial( { map: floorTexture, bumpMap: floorTexture, bumpScale: 0.2, metalness: 0.5 } )
            );
            labyrinthPlane.receiveShadow = true;
            _scene.add(labyrinthPlane);
            
            let walls = [];

            for (let i = -_halfLabyrinthSize; i < _halfLabyrinthSize; i++) {
                var northLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 1, 0.1, 0.5 ), labyrinthWallMaterial );
                northLabyrinthWall.position.set(i + 0.5, -_halfLabyrinthSize, 0.24);
                northLabyrinthWall.receiveShadow = true;
                northLabyrinthWall.castShadow = true;
                _scene.add( northLabyrinthWall );
                
                var eastLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, 1, 0.5 ), labyrinthWallMaterial );
                eastLabyrinthWall.position.set(_halfLabyrinthSize, i + 0.5, 0.24);
                eastLabyrinthWall.receiveShadow = true;
                eastLabyrinthWall.castShadow = true;
                _scene.add( eastLabyrinthWall );

                var southLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 1, 0.1, 0.5 ), labyrinthWallMaterial );
                southLabyrinthWall.position.set(i + 0.5, _halfLabyrinthSize, 0.24);
                southLabyrinthWall.receiveShadow = true;
                southLabyrinthWall.castShadow = true;
                _scene.add( southLabyrinthWall );

                var westLabyrinthWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, 1, 0.5 ), labyrinthWallMaterial );
                westLabyrinthWall.position.set(-_halfLabyrinthSize, i + 0.5, 0.24);
                westLabyrinthWall.receiveShadow = true;
                westLabyrinthWall.castShadow = true;
                _scene.add( westLabyrinthWall );
                
            }
            
            var labyrinthCells = [];

            for (var y = 0; y < _labyrinthSize; y++) {

                labyrinthCells.push([]);

                for (var x = 0; x < _labyrinthSize; x++) {

                    var ownerMarker = new THREE.Mesh( new THREE.CircleGeometry(0.3, 16), new THREE.MeshStandardMaterial({color: 0x999999, transparent: true, opacity: 0.0, bumpMap: ownerMarkerFloorTexture, bumpScale: 0.2}));
                    ownerMarker.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y + 0.5, 0.01);
                    ownerMarker.receiveShadow = true;
                    ownerMarker.targetOpacity = 0.0;
                    ownerMarker.targetColor = new THREE.Color(0x999999);
                    _scene.add( ownerMarker );
   
                    var visitableMarker = new THREE.Mesh( new THREE.ConeBufferGeometry(0.25, 0.5, 16), new THREE.MeshStandardMaterial({map: wallTexture, bumpMap: wallTexture, bumpScale: 0.4}));
                    visitableMarker.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y + 0.5, -0.5);
                    visitableMarker.rotation.x = Math.PI / 2;
                    visitableMarker.visible = false;
                    visitableMarker.receiveShadow = true;
                    visitableMarker.castShadow = true;
                    visitableMarker.targetPositionZ = -0.5;
                    _scene.add( visitableMarker );

                    labyrinthCells[y].push({
                       ownerMarker: ownerMarker,
                        visitableMarker: visitableMarker
                    });

                    var cell = gameData.labyrinth.cells[y][x];

                    if (cell.walls[0] && y !== 0) {
                        var northCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 1, 0.1, 0.5 ), labyrinthWallMaterial );
                        northCellWall.position.set(-_halfLabyrinthSize + x + 0.5, -_halfLabyrinthSize + y, 0.24);
                        northCellWall.receiveShadow = true;
                        northCellWall.castShadow = true;
                        _scene.add( northCellWall );
                    }

                    if (cell.walls[3] && x !== 0) {
                        var westCellWall = new THREE.Mesh( new THREE.BoxBufferGeometry( 0.1, 1, 0.5 ), labyrinthWallMaterial );
                        westCellWall.position.set(-_halfLabyrinthSize + x, -_halfLabyrinthSize + y + 0.5, 0.24);
                        westCellWall.receiveShadow = true;
                        westCellWall.castShadow = true;
                        _scene.add( westCellWall );
                    }

                }
            }

            return labyrinthCells;
        }

        // Start game loop
        
        let _this = this;

        function animate() {
            
            stats.begin();

            _players.forEach(function(player) {
                var move = player.targetPosition.clone().sub(player.position).multiplyScalar(0.12);
                player.position.copy(player.position.add(move));
                player.rotation.z -= 0.02;
            });

            for (var y = 0; y < _labyrinthSize; y++) {
                for (var x = 0; x < _labyrinthSize; x++) {
                    var lc = _labyrinthCells[y][x];
                    lc.ownerMarker.material.color.lerp(lc.ownerMarker.targetColor, 0.01);
                    lc.ownerMarker.material.opacity += (lc.ownerMarker.targetOpacity - lc.ownerMarker.material.opacity) * 0.03;
                    lc.visitableMarker.position.z += (lc.visitableMarker.targetPositionZ - lc.visitableMarker.position.z) * 0.03;
                }
            }
            
            if (_this.canMove && _mainLight1.power > mainLightMinPower) {
                _mainLight1.power -= 0.03; 
            }
            if (_this.canMove && _mainLight2.power > mainLightMinPower) {
                _mainLight2.power -= 0.03; 
            }
            
            if (!_this.canMove && _mainLight1.power < mainLightMaxPower) {
                _mainLight1.power += 0.03; 
            }
            if (!_this.canMove && _mainLight2.power < mainLightMaxPower) {
                _mainLight2.power += 0.03; 
            }

            _renderer.render( _scene, _camera );
            
            stats.end();
            
            requestAnimationFrame( animate );
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
                let lc = this.labyrinthCells[y][x];
                lc.ownerMarker.targetColor.set(this.colors[gameState.cellsOwnerIndices[y][x]]);
                lc.ownerMarker.targetOpacity = gameState.cellsOwnerIndices[y][x] === -1 ? 0.0 : 1.0;              
                lc.visitableMarker.visible = !gameState.cellsVisitable[y][x];
                lc.visitableMarker.targetPositionZ = gameState.cellsVisitable[y][x] ? -0.25 : 0.25;
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

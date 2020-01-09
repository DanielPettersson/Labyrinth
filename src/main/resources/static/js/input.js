class InputHandler {

    constructor(game) {

        window.addEventListener( 'resize', function() {
            game.resize(window.innerWidth, window.innerHeight)
        }, false );


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
            game.doMove(move);

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
                    game.doMove({ x: -1, y: 0});
                } else {
                    doMove({ x: 1, y: 0});
                }
            } else {
                if ( yDiff > 0 ) {
                    game.doMove({ x: 0, y: 1});
                } else {
                    game.doMove({ x: 0, y: -1});
                }
            }

            xDown = null;
            yDown = null;
        }

    }

}
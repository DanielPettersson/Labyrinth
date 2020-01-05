package se.daniel.labyrinth.controller;

import io.javalin.websocket.WsContext;
import lombok.AllArgsConstructor;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;

import java.util.UUID;

@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    public void joinGameRequest(GameSpecification gameSpecification, WsContext wsContext) {

        final JoinInfo joinInfo = gameService.joinGame(gameSpecification, wsContext);

        // Notify other waiting players that game started

        if (joinInfo.getGame() != null) {

            joinInfo.getGame()
                    .getPlayers()
                    .stream()
                    .filter(p -> !p.getWsContext().equals(wsContext))
                    .forEach(p -> p.getWsContext().send(new Command<>("started", joinInfo.getGame())));
        }

        // Send response to player joining game

        wsContext.send(new Command<>("joined", joinInfo));
    }

    public void movePlayer(Move move, WsContext wsContext) {

        final var gameUuid = UUID.fromString(move.getGameId());

        // If valid move, send new state to players

        if (gameService.movePlayer(gameUuid, move.getTo(), wsContext)) {
            gameService.getPlayerWsContexts(gameUuid).forEach(
                    ctx -> ctx.send(new Command<>("state", gameService.getGameState(gameUuid, ctx)))
            );
        }

        // Check if game ended and send commands if it did

        final var gameEndedOpt = gameService.getGameEnded(gameUuid);
        gameEndedOpt.ifPresent(gameEnded ->
            gameService.getPlayerWsContexts(gameUuid).forEach(
                    ctx -> ctx.send(new Command<>("ended", gameEnded))
            )
        );
    }

    public void playerDisconnected(WsContext wsContext) {

        gameService.getGames(wsContext).forEach(game -> {

            final var gameEnded = GameEnded.fromGame(game);
            gameService.getPlayerWsContexts(game.getUuid()).forEach(
                    ctx -> ctx.send(new Command<>("ended", gameEnded))
            );

            gameService.endGame(game);
        });

        gameService.removePlayerFromRequest(wsContext);

    }
}

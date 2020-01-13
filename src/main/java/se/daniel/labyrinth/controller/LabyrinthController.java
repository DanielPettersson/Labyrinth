package se.daniel.labyrinth.controller;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import se.daniel.labyrinth.model.GameEnded;
import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Move;
import se.daniel.labyrinth.engine.LabyrinthEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LabyrinthController {

    private final LabyrinthEngine labyrinthEngine;
    private final CommandHandler commandHandler;
    private final Map<String, WsContext> players = new ConcurrentHashMap<>();

    public LabyrinthController(final LabyrinthEngine labyrinthEngine, final CommandHandler commandHandler) {

        this.labyrinthEngine = labyrinthEngine;
        this.commandHandler = commandHandler;

        commandHandler.addDispatchCommand("join", (mapper, message, wsContext) -> {
            final var gameSpecification = mapper.readValue(message, GameSpecification.class);
            joinGameRequest(gameSpecification, wsContext);
        });

        commandHandler.addDispatchCommand("move", (mapper, message, wsContext) -> {
            final var move = mapper.readValue(message, Move.class);
            movePlayer(move, wsContext);
        });
    }

    public void playerDisconnected(WsCloseContext wsCloseContext) {

        final var playerId = wsCloseContext.getSessionId();
        players.remove(playerId);

        labyrinthEngine.getGames(playerId).forEach(game -> {

            labyrinthEngine.playerQuit(game.getUuid(), playerId);
            
            final var gameEnded = GameEnded.fromGame(game);
            labyrinthEngine.getPlayerIds(game.getUuid())
                    .stream()
                    .filter(p -> !p.equals(playerId))
                    .forEach(
                            id -> {
                                final var gameState = labyrinthEngine.getGameState(game.getUuid(), id);
                                commandHandler.sendCommand(players.get(id), "state", gameState);
                                commandHandler.sendCommand(players.get(id), "ended", gameEnded);
                            }
                    );

            labyrinthEngine.endGame(game);
        });

        labyrinthEngine.removePlayerFromRequest(playerId);

    }

    public void playerConnected(WsConnectContext wsConnectContext) {
        players.put(wsConnectContext.getSessionId(), wsConnectContext);
    }

    private void joinGameRequest(GameSpecification gameSpecification, WsContext wsContext) {

        final var playerId = wsContext.getSessionId();
        final JoinInfo joinInfo = labyrinthEngine.joinGame(gameSpecification, playerId);

        // Notify other waiting players that game started

        if (joinInfo.getGame() != null) {

            joinInfo.getGame()
                    .getPlayers()
                    .stream()
                    .filter(p -> !p.getId().equals(playerId))
                    .forEach(p -> commandHandler.sendCommand(players.get(p.getId()),"started", joinInfo.getGame()));
        }

        // Send response to player joining game

        commandHandler.sendCommand(wsContext, "joined", joinInfo);
    }

    private void movePlayer(Move move, WsContext wsContext) {

        final var playerId = wsContext.getSessionId();
        final var gameUuid = UUID.fromString(move.getGameId());

        // If valid move, send new state to players

        if (labyrinthEngine.movePlayer(gameUuid, move.getTo(), playerId)) {
            labyrinthEngine.getPlayerIds(gameUuid).forEach(
                    id -> {
                        final var gameState = labyrinthEngine.getGameState(gameUuid, id);
                        commandHandler.sendCommand(players.get(id), "state", gameState);
                    }
            );
        }

        // Check if game ended and send commands if it did

        final var gameEndedOpt = labyrinthEngine.getGameEnded(gameUuid);
        gameEndedOpt.ifPresent(gameEnded ->
            labyrinthEngine.getPlayerIds(gameUuid).forEach(
                    id -> commandHandler.sendCommand(players.get(id), "ended", gameEnded)
            )
        );
    }


}

package se.daniel.labyrinth.controller;

import lombok.AllArgsConstructor;
import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Move;
import se.daniel.labyrinth.service.GameService;

import java.util.UUID;

@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    public JoinInfo joinGameRequest(GameSpecification gameSpecification) {

        final JoinInfo joinInfo = gameService.joinGame(gameSpecification);

        if (joinInfo.getGame() != null) {
            //messagingTemplate.convertAndSend("/topic/game-started/" + joinInfo.getGameUuid(), joinInfo.getGame());
        }

        return joinInfo;
    }

    public void movePlayer(Move move) {

        final var gameUuid = UUID.fromString(move.getGameId());

        if (gameService.movePlayer(gameUuid, UUID.fromString(move.getPlayerId()), move.getTo())) {

            /*
            gameService.getPlayers(gameUuid).forEach(
                    playerUuid -> messagingTemplate.convertAndSend(
                            "/topic/player-moved/" + gameId + "/" + playerUuid.toString(),
                            gameService.getGameState(gameUuid, playerUuid)
                    )
            );
            */

        }

        final var gameEndedOpt = gameService.getGameEnded(gameUuid);
//        gameEndedOpt.ifPresent(gameEnded -> messagingTemplate.convertAndSend("/topic/game-ended/" + gameUuid, gameEnded));
    }

    private void removeTimedOutGameRequests() {
  /*
        gameService.removeTimedOutGameRequests().forEach(
                gameUuid -> messagingTemplate.convertAndSend("/topic/game-request-aborted/" + gameUuid, "")
        );

   */
    }

    public String handleException(Exception e) {
        return e.getMessage();
    }

}

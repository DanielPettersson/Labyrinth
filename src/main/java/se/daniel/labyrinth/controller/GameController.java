package se.daniel.labyrinth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.GameState;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Location;
import se.daniel.labyrinth.service.GameService;

import java.util.UUID;

@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(@Autowired GameService gameService, @Autowired SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join-game")
    @SendToUser("/topic/game-joined")
    public JoinInfo joinGameRequest(GameSpecification gameSpecification) {

        final JoinInfo joinInfo = gameService.joinGame(gameSpecification);

        if (joinInfo.getGame() != null) {
            messagingTemplate.convertAndSend("/topic/game-started/" + joinInfo.getGameUuid(), joinInfo.getGame());
        }

        return joinInfo;
    }

    @MessageMapping("/move-player/{gameId}/{playerId}")
    @SendTo("/topic/player-moved/{gameId}")
    public GameState movePlayer(
            @DestinationVariable String gameId,
            @DestinationVariable String playerId,
            Location move) {
        return gameService.movePlayer(UUID.fromString(gameId), UUID.fromString(playerId), move);
    }

    @Scheduled(fixedRate = 1000)
    private void removeTimedOutGameRequests() {
        gameService.removeTimedOutGameRequests().forEach(
                gameUuid -> messagingTemplate.convertAndSend("/topic/game-request-aborted/" + gameUuid, "")
        );
    }

}

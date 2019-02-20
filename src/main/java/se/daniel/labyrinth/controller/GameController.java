package se.daniel.labyrinth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Location;
import se.daniel.labyrinth.model.Player;
import se.daniel.labyrinth.service.GameService;

import java.util.List;
import java.util.UUID;

@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(@Autowired final GameService gameService, @Autowired final SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join-game/{numPlayers}")
    @SendToUser("/topic/game-joined")
    public JoinInfo joinGameRequest(@DestinationVariable int numPlayers) {

        final JoinInfo joinInfo = gameService.joinGame(numPlayers);

        if (joinInfo.getGame() != null) {
            messagingTemplate.convertAndSend("/topic/game-started/" + joinInfo.getGameUuid(), joinInfo.getGame());
        }

        return joinInfo;
    }

    @MessageMapping("/move-player/{gameId}/{playerId}")
    @SendTo("/topic/player-moved/{gameId}")
    public List<Player> movePlayer(
            @DestinationVariable String gameId,
            @DestinationVariable String playerId,
            Location move) {
        return gameService.movePlayer(UUID.fromString(gameId), UUID.fromString(playerId), move);
    }

    @Scheduled(fixedRate = 1000)
    private void removeTimedOutGameRequests() {
        gameService.removeTimedOutGameRequests().forEach(
                r -> messagingTemplate.convertAndSend("/topic/game-request-aborted/" + r.getGameUuid(), "")
        );
    }

}

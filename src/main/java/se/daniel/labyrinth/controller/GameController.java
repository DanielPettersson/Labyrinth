package se.daniel.labyrinth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;

import java.util.List;
import java.util.UUID;

@Controller
public class GameController {

    private static final String TOPIC_GAME_REQUESTS = "/topic/game-requests";

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(@Autowired final GameService gameService, @Autowired final SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/create-game-request")
    @SendToUser("/topic/game-request-created")
    public PlayerInfo createGameRequest() {
        final var playerInfo = gameService.createGameRequest();
        updateGameRequests();
        return playerInfo;
    }

    @MessageMapping("/get-game-requests")
    @SendToUser(TOPIC_GAME_REQUESTS)
    public List<PublicGameRequest> getGameRequests() {
        return gameService.getGameRequests();
    }

    @MessageMapping("/join-game-request/{gameId}")
    @SendToUser("/topic/game-request-joined")
    public PlayerInfo joinGameRequest(@DestinationVariable String gameId) {
        return gameService.joinGameRequest(UUID.fromString(gameId));
    }

    @MessageMapping("/start-game/{gameId}")
    @SendTo("/topic/game-started/{gameId}")
    public Game startGame(@DestinationVariable String gameId) {
        final Game game = gameService.startGame(UUID.fromString(gameId));
        updateGameRequests();
        return game;
    }

    @MessageMapping("/move-player/{gameId}/{playerId}")
    @SendTo("/topic/player-moved/{gameId}")
    public List<Player> movePlayer(
            @DestinationVariable String gameId,
            @DestinationVariable String playerId,
            Location move) {
        return gameService.movePlayer(UUID.fromString(gameId), UUID.fromString(playerId), move);
    }

    @Scheduled(fixedRate = 2000)
    public void removeOldRequests() {

        gameService.removeTimedOutGameRequests()
                .forEach(
                        removedGameRequest -> messagingTemplate.convertAndSend(
                                "/topic/game-aborted/" + removedGameRequest.getGameUuid(),
                                ""
                        )
                );

        updateGameRequests();
    }

    private void updateGameRequests() {
        messagingTemplate.convertAndSend(TOPIC_GAME_REQUESTS, gameService.getGameRequests());
    }

}

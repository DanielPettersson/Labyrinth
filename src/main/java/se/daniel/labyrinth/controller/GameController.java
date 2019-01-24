package se.daniel.labyrinth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import se.daniel.labyrinth.model.Game;
import se.daniel.labyrinth.model.GameRequest;
import se.daniel.labyrinth.service.GameService;

import java.util.List;

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
    public GameRequest createGameRequest() {
        final GameRequest gameRequest = gameService.createGameRequest();
        updateGameRequests();
        return gameRequest;
    }

    @MessageMapping("/get-game-requests")
    @SendToUser(TOPIC_GAME_REQUESTS)
    public List<GameRequest> getGameRequests() {
        return gameService.getGameRequests();
    }

    @MessageMapping("/start-game/{gameId}")
    @SendTo("/topic/game-started/{gameId}")
    public Game startGame(@DestinationVariable String gameId) {
        final Game game = gameService.startGame(gameId);
        updateGameRequests();
        return game;
    }

    @Scheduled(fixedRate = 2000)
    public void removeOldRequests() {

        gameService.removeTimedOutGameRequests()
                .forEach(removedGameRequest -> messagingTemplate.convertAndSend("/topic/game-aborted/" + removedGameRequest.getUuid(), ""));

        updateGameRequests();
    }

    private void updateGameRequests() {
        messagingTemplate.convertAndSend(TOPIC_GAME_REQUESTS, gameService.getGameRequests());
    }

}

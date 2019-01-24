package se.daniel.labyrinth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import se.daniel.labyrinth.service.GameService;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(@Autowired final GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/creategame")
    @SendTo("/topic/newgame")
    public String createGame() {
        return gameService.createGame();
    }

}

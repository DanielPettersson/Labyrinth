package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.Game;
import se.daniel.labyrinth.model.GameRequest;

import java.util.List;

public interface GameService {

    GameRequest createGameRequest();

    List<GameRequest> getGameRequests();

    List<GameRequest> removeTimedOutGameRequests();

    Game startGame(String uuid);


}

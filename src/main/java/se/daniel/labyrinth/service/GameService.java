package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.Game;
import se.daniel.labyrinth.model.GameRequest;
import se.daniel.labyrinth.model.JoinGameRequest;
import se.daniel.labyrinth.model.PublicGameRequest;

import java.util.List;

public interface GameService {

    GameRequest createGameRequest();

    List<PublicGameRequest> getGameRequests();

    List<GameRequest> removeTimedOutGameRequests();

    JoinGameRequest joinGame(String uuid);

    Game startGame(String uuid);


}

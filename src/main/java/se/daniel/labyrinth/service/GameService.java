package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.*;

import java.util.List;
import java.util.UUID;

public interface GameService {

    GameRequest createGameRequest();

    List<PublicGameRequest> getGameRequests();

    List<GameRequest> removeTimedOutGameRequests();

    JoinGameRequest joinGame(UUID uuid);

    Game startGame(UUID uuid);

    List<Player> movePlayer(UUID gameId, UUID playerId, Location move);
}

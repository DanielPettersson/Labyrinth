package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.*;

import java.util.List;
import java.util.UUID;

public interface GameService {

    PlayerInfo createGameRequest();

    List<PublicGameRequest> getGameRequests();

    List<GameRequest> removeTimedOutGameRequests();

    PlayerInfo joinGameRequest(UUID uuid);

    Game startGame(UUID uuid);

    List<Player> movePlayer(UUID gameId, UUID playerId, Location move);
}

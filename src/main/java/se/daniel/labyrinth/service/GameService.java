package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameService {

    JoinInfo joinGame(GameSpecification gameSpecification);

    boolean movePlayer(UUID gameId, UUID playerId, Location move);

    List<UUID> getPlayers(UUID gameId);

    GameState getGameState(UUID gameId, UUID playerId);

    Optional<GameEnded> getGameEnded(UUID gameId);

    List<UUID> removeTimedOutGameRequests();
}

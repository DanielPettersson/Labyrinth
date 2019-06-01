package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.GameState;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Location;

import java.util.List;
import java.util.UUID;

public interface GameService {

    JoinInfo joinGame(GameSpecification gameSpecification);

    boolean movePlayer(UUID gameId, UUID playerId, Location move);

    List<UUID> getPlayers(UUID gameId);

    GameState getGameState(UUID gameId, UUID playerId);

    List<UUID> removeTimedOutGameRequests();
}

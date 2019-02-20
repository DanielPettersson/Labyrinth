package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.GameRequest;
import se.daniel.labyrinth.model.GameState;
import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Location;

import java.util.List;
import java.util.UUID;

public interface GameService {

    JoinInfo joinGame(int numPlayers);

    GameState movePlayer(UUID gameId, UUID playerId, Location move);

    List<GameRequest> removeTimedOutGameRequests();
}

package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.JoinInfo;
import se.daniel.labyrinth.model.Location;
import se.daniel.labyrinth.model.Player;

import java.util.List;
import java.util.UUID;

public interface GameService {

    JoinInfo joinGame(int numPlayers);

    List<Player> movePlayer(UUID gameId, UUID playerId, Location move);
}

package se.daniel.labyrinth.engine;

import se.daniel.labyrinth.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabyrinthEngine {

    JoinInfo joinGame(GameSpecification gameSpecification, String playerId);

    boolean movePlayer(UUID gameId, Location move, String playerId);
    
    void playerQuit(UUID gameId, String playerId);

    GameState getGameState(UUID gameId, String playerId);

    List<String> getPlayerIds(UUID gameId);

    List<Game> getGames(String playerId);

    void endGame(Game game);

    Optional<GameEnded> getGameEnded(UUID gameId);

    void removePlayerFromRequest(String playerId);

}

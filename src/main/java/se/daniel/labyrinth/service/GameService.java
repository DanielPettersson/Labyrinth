package se.daniel.labyrinth.service;

import io.javalin.websocket.WsContext;
import se.daniel.labyrinth.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameService {

    JoinInfo joinGame(GameSpecification gameSpecification, WsContext wsContext);

    boolean movePlayer(UUID gameId, Location move, WsContext wsContext);

    GameState getGameState(UUID gameId, WsContext wsContext);

    List<WsContext> getPlayerWsContexts(UUID gameId);

    List<Game> getGames(WsContext wsContext);

    void endGame(Game game);

    Optional<GameEnded> getGameEnded(UUID gameId);

    void removePlayerFromRequest(WsContext wsContext);

}

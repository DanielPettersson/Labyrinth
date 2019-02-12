package se.daniel.labyrinth.service.impl;

import org.springframework.stereotype.Service;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class GameServiceImpl implements GameService {

    private final Map<UUID, Game> games = new HashMap<>();

    private final Map<UUID, GameRequest> gameRequests = new LinkedHashMap<>();

    @Override
    public GameRequest createGameRequest() {
        final GameRequest gameRequest = new GameRequest();
        gameRequests.put(gameRequest.getGameUuid(), gameRequest);
        return gameRequest;
    }

    public List<PublicGameRequest> getGameRequests() {
        return gameRequests.values().stream().map(PublicGameRequest::new).collect(toList());
    }

    @Override
    public List<GameRequest> removeTimedOutGameRequests() {
        final LocalDateTime now = LocalDateTime.now();
        final List<GameRequest> oldRequests = gameRequests
                .values()
                .stream()
                .filter(r -> Duration.between(r.getCreationDate(), now).getSeconds() > 60)
                .collect(toList());

        oldRequests.forEach(r -> gameRequests.remove(r.getGameUuid()));
        return oldRequests;
    }

    @Override
    public JoinGameRequest joinGame(String uuid) {
        final GameRequest gameRequest = gameRequests.get(UUID.fromString(uuid));
        final UUID newUuid = gameRequest.addPlayer();
        return new JoinGameRequest(newUuid);
    }

    @Override
    public Game startGame(String uuid) {
        gameRequests.remove(UUID.fromString(uuid));
        var game = new Game(
                UUID.fromString(uuid),
                new LabyrinthBuilder(new Random()).build(10)
        );
        games.put(game.getUuid(), game);
        return game;
    }

}

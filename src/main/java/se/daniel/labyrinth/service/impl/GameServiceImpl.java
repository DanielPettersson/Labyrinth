package se.daniel.labyrinth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class GameServiceImpl implements GameService {

    private static final int DEFAULT_GAME_SIZE = 10;

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
    public JoinGameRequest joinGame(UUID uuid) {
        final GameRequest gameRequest = gameRequests.get(uuid);
        final UUID newUuid = gameRequest.addPlayer();
        return new JoinGameRequest(newUuid);
    }

    @Override
    public Game startGame(UUID uuid) {
        final GameRequest gameRequest = gameRequests.remove(uuid);

        Assert.isTrue(gameRequest.getPlayerUuids().size() == 2, "Currently only 2 players is supported");
        var players = new ArrayList<Player>();
        players.add(new Player(gameRequest.getPlayerUuids().get(0), new Location(0, 0)));
        players.add(new Player(gameRequest.getPlayerUuids().get(1), new Location(DEFAULT_GAME_SIZE - 1, DEFAULT_GAME_SIZE - 1)));

        var game = new Game(
                uuid,
                new LabyrinthBuilder(new Random()).build(DEFAULT_GAME_SIZE),
                players
        );
        games.put(game.getUuid(), game);
        return game;
    }

    @Override
    public List<Player> movePlayer(UUID gameId, UUID playerId, Location move) {

        final Game game = games.get(gameId);

        if (Math.abs(move.getX() + move.getY()) == 1) {
            game.getPlayers()
                    .stream()
                    .filter(p -> playerId.equals(p.getUuid()))
                    .forEach(p -> p.setLocation(p.getLocation().add(move)));
        }

        return game.getPlayers();
    }

}

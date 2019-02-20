package se.daniel.labyrinth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Service
public class GameServiceImpl implements GameService {

    private static final int DEFAULT_GAME_SIZE = 10;

    private final Map<UUID, Game> games = new HashMap<>();
    private final Map<Integer, GameRequest> gameRequests = new ConcurrentHashMap<>();

    @Override
    public JoinInfo joinGame(int numPlayers) {

        Assert.isTrue(numPlayers >= 2, "Has to be at least one player");
        Assert.isTrue(numPlayers <= 4, "Max four players supported");

        final GameRequest gameRequest = gameRequests.computeIfAbsent(numPlayers, n -> new GameRequest());
        final UUID playerUuid = gameRequest.addPlayer();
        final Game game = gameRequest.getPlayerUuids().size() == numPlayers ? startGame(numPlayers) : null;

        return new JoinInfo(
                gameRequest.getGameUuid(),
                playerUuid,
                gameRequest.getPlayerUuids().size() - 1,
                game
        );
    }

    private Game startGame(int numPlayers) {
        final var gameRequest = gameRequests.remove(numPlayers);

        var players = new ArrayList<Player>();
        players.add(new Player(gameRequest.getPlayerUuids().get(0), new Location(0, 0)));
        players.add(new Player(gameRequest.getPlayerUuids().get(1), new Location(DEFAULT_GAME_SIZE - 1, DEFAULT_GAME_SIZE - 1)));
        if (numPlayers >= 3) {
            players.add(new Player(gameRequest.getPlayerUuids().get(2), new Location(0, DEFAULT_GAME_SIZE - 1)));
        }
        if (numPlayers == 4) {
            players.add(new Player(gameRequest.getPlayerUuids().get(3), new Location(DEFAULT_GAME_SIZE - 1, 0)));
        }

        var game = new Game(
                gameRequest.getGameUuid(),
                new LabyrinthBuilder(new Random()).build(DEFAULT_GAME_SIZE),
                players
        );
        games.put(game.getUuid(), game);
        return game;
    }

    @Override
    public GameState movePlayer(UUID gameId, UUID playerId, Location move) {

        final Game game = games.get(gameId);

        if (Math.abs(move.getX() + move.getY()) == 1) {
            game.getPlayers()
                    .stream()
                    .filter(p -> playerId.equals(p.getUuid()))
                    .filter(p -> game.isValidMove(p, move))
                    .forEach(p -> {
                        final Location to = p.getLocation().add(move);
                        final int ownerIndex = game.getPlayers().indexOf(p);
                        game.getLabyrinth().getCell(to).setOwnerIndex(ownerIndex);
                        p.setLocation(to);
                    });
        }

        final List<List<Integer>> cellOwnerIndices = game
                .getLabyrinth()
                .getCells()
                .stream()
                .map(
                        cells -> cells
                                .stream()
                                .map(Cell::getOwnerIndex)
                                .collect(toList())
                )
                .collect(toList());


        return new GameState(
                cellOwnerIndices,
                game.getPlayers()
        );
    }

    @Override
    public List<GameRequest> removeTimedOutGameRequests() {
        final LocalDateTime now = LocalDateTime.now();
        final List<Integer> oldKeys = gameRequests
                .entrySet()

                .stream()
                .filter(r -> Duration.between(r.getValue().getCreationDate(), now).getSeconds() > 30)
                .map(Map.Entry::getKey)
                .collect(toList());

        return oldKeys.stream().map(gameRequests::remove).collect(toList());
    }

}

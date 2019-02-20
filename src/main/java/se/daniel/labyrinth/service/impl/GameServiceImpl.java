package se.daniel.labyrinth.service.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;
import se.daniel.labyrinth.util.LabyrinthBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Service
public class GameServiceImpl implements GameService {

    private static final int GAME_REQUEST_TTL_SECONDS = 30;

    private final Map<UUID, Game> games = new HashMap<>();
    private final Map<GameSpecification, GameRequest> gameRequests = new ConcurrentHashMap<>();

    @Override
    public JoinInfo joinGame(GameSpecification gameSpecification) {

        final int numPlayers = gameSpecification.getNumPlayers();

        Assert.isTrue(numPlayers >= 2, "Has to be at least one player");
        Assert.isTrue(numPlayers <= 4, "Max four players supported");
        Assert.isTrue(gameSpecification.getGameSize() > 1, "Game has to have a size");

        final GameRequest gameRequest = gameRequests.computeIfAbsent(gameSpecification, n -> new GameRequest());
        final UUID playerUuid = gameRequest.addPlayer();
        final Game game = gameRequest.getPlayerUuids().size() == numPlayers ? startGame(gameSpecification) : null;

        return new JoinInfo(
                gameRequest.getGameUuid(),
                playerUuid,
                gameRequest.getPlayerUuids().size() - 1,
                game
        );
    }

    private Game startGame(GameSpecification gameSpecification) {
        final int numPlayers = gameSpecification.getNumPlayers();
        final int gameSize = gameSpecification.getGameSize();

        final var gameRequest = gameRequests.remove(gameSpecification);

        var players = new ArrayList<Player>();
        players.add(new Player(gameRequest.getPlayerUuids().get(0), new Location(0, 0)));
        players.add(new Player(gameRequest.getPlayerUuids().get(1), new Location(gameSize - 1, gameSize - 1)));
        if (numPlayers >= 3) {
            players.add(new Player(gameRequest.getPlayerUuids().get(2), new Location(0, gameSize - 1)));
        }
        if (numPlayers == 4) {
            players.add(new Player(gameRequest.getPlayerUuids().get(3), new Location(gameSize - 1, 0)));
        }

        var game = new Game(
                gameRequest.getGameUuid(),
                new LabyrinthBuilder(new Random()).build(gameSize),
                players
        );

        players.forEach(
                p -> game
                        .getLabyrinth()
                        .getCell(p.getLocation())
                        .setOwnerIndex(game.getPlayers().indexOf(p))
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
    public List<UUID> removeTimedOutGameRequests() {
        final LocalDateTime now = LocalDateTime.now();
        final List<GameSpecification> oldKeys = gameRequests
                .entrySet()

                .stream()
                .filter(r -> Duration.between(r.getValue().getCreationDate(), now).getSeconds() > GAME_REQUEST_TTL_SECONDS)
                .map(Map.Entry::getKey)
                .collect(toList());

        return oldKeys.stream().map(gameRequests::remove).map(GameRequest::getGameUuid).collect(toList());
    }

    @Getter
    @EqualsAndHashCode(of = {"gameUuid"})
    private static class GameRequest {

        private final LocalDateTime creationDate;
        private final UUID gameUuid;
        private final List<UUID> playerUuids = new ArrayList<>();

        GameRequest() {
            this.gameUuid = UUID.randomUUID();
            this.creationDate = LocalDateTime.now();
        }

        UUID addPlayer() {
            final UUID uuid = UUID.randomUUID();
            playerUuids.add(uuid);
            return uuid;
        }

    }
}

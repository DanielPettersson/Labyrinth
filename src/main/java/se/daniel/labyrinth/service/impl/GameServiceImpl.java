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
    private static final List<Location> VALID_MOVES = List.of(new Location(0, 1), new Location(0, -1), new Location(1, 0), new Location(-1, 0));

    private final Map<UUID, Game> games = new HashMap<>();
    private final Map<GameSpecification, GameRequest> gameRequests = new ConcurrentHashMap<>();

    @Override
    public JoinInfo joinGame(GameSpecification gameSpecification) {

        final int numPlayers = gameSpecification.getNumPlayers();

        Assert.isTrue(numPlayers >= 2, "Has to be at least one player");
        Assert.isTrue(numPlayers <= 4, "Max four players supported");
        Assert.isTrue(gameSpecification.getGameSize() >= 2, "Too small game size");
        Assert.isTrue(gameSpecification.getGameSize() <= 10, "Too large game size");

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
                        .playerVisit(game.getPlayers().indexOf(p))
        );

        games.put(game.getUuid(), game);
        return game;
    }

    @Override
    public boolean movePlayer(UUID gameId, UUID playerId, Location move) {

        final Game game = games.get(gameId);
        final var player = game.getPlayerByUUid(playerId);

        if (game.isValidMove(player, move)) {

            final Location to = player.getLocation().add(move);
            final int ownerIndex = game.getPlayers().indexOf(player);
            game.getLabyrinth().getCell(to).playerVisit(ownerIndex);
            player.setLocation(to);

            return true;

        } else {

            return false;
        }

    }

    @Override
    public List<UUID> getPlayers(UUID gameId) {
        return games.get(gameId).getPlayers().stream().map(Player::getUuid).collect(toList());
    }

    @Override
    public GameState getGameState(UUID gameId, UUID playerId) {
        final var game = games.get(gameId);
        final var player = game.getPlayerByUUid(playerId);
        final var playerIndex = game.getPlayers().indexOf(player);

        final var cellOwnerIndices = game.getLabyrinth().getCells().stream().map(
                cells -> cells.stream().map(Cell::getOwnerIndex).collect(toList())
        ).collect(toList());

        final var cellsVisitable = game.getLabyrinth().getCells().stream().map(
                cells -> cells.stream().map(c -> c.canVisit(playerIndex)).collect(toList())
        ).collect(toList());

        return new GameState(
                cellOwnerIndices,
                cellsVisitable,
                game.getPlayers()
        );
    }

    @Override
    public Optional<GameEnded> getGameEnded(UUID gameId) {

        final var game = games.get(gameId);
        final var allCellsOwned = game.getLabyrinth().isAllCellsOwned();
        final var noPlayerHasValidMove = game.getPlayers().stream().noneMatch(p -> VALID_MOVES.stream().anyMatch(m -> game.isValidMove(p, m)));

        if (allCellsOwned || noPlayerHasValidMove) {
            return Optional.of(GameEnded.fromGame(game));
        } else {
            return Optional.empty();
        }

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

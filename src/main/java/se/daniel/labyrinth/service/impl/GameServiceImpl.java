package se.daniel.labyrinth.service.impl;

import io.javalin.websocket.WsContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import se.daniel.labyrinth.model.*;
import se.daniel.labyrinth.service.GameService;
import se.daniel.labyrinth.util.Assert;
import se.daniel.labyrinth.util.LabyrinthBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Slf4j
public class GameServiceImpl implements GameService {

    private static final List<Location> VALID_MOVES = List.of(new Location(0, 1), new Location(0, -1), new Location(1, 0), new Location(-1, 0));

    private final Map<UUID, Game> games = new HashMap<>();
    private final Map<GameSpecification, GameRequest> gameRequests = new ConcurrentHashMap<>();

    @Override
    public JoinInfo joinGame(GameSpecification gameSpecification, WsContext wsContext) {

        final int numPlayers = gameSpecification.getNumPlayers();

        Assert.isTrue(numPlayers >= 2, "Has to be at least one player");
        Assert.isTrue(numPlayers <= 4, "Max four players supported");
        Assert.isTrue(gameSpecification.getGameSize() >= 2, "Too small game size");
        Assert.isTrue(gameSpecification.getGameSize() <= 10, "Too large game size");

        final GameRequest gameRequest = gameRequests.computeIfAbsent(gameSpecification, n -> new GameRequest());
        gameRequest.addPlayer(wsContext);
        final Game game = gameRequest.getPlayers().size() == numPlayers ? startGame(gameSpecification) : null;

        return new JoinInfo(
                gameRequest.getGameUuid(),
                gameRequest.getPlayers().size() - 1,
                game
        );
    }

    private Game startGame(GameSpecification gameSpecification) {
        final int numPlayers = gameSpecification.getNumPlayers();
        final int gameSize = gameSpecification.getGameSize();

        final var gameRequest = gameRequests.remove(gameSpecification);

        var players = new ArrayList<Player>();
        players.add(new Player(gameRequest.getPlayers().get(0), new Location(0, 0)));
        players.add(new Player(gameRequest.getPlayers().get(1), new Location(gameSize - 1, gameSize - 1)));
        if (numPlayers >= 3) {
            players.add(new Player(gameRequest.getPlayers().get(2), new Location(0, gameSize - 1)));
        }
        if (numPlayers == 4) {
            players.add(new Player(gameRequest.getPlayers().get(3), new Location(gameSize - 1, 0)));
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
    public boolean movePlayer(UUID gameId, Location move, WsContext wsContext) {

        final Game game = games.get(gameId);
        final var player = game.getPlayerWsContext(wsContext);

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
    public List<WsContext> getPlayerWsContexts(UUID gameId) {
        if (games.containsKey(gameId)) {
            return games.get(gameId).getPlayers().stream().map(Player::getWsContext).collect(toList());
        } else {
            return List.of();
        }
    }

    @Override
    public List<Game> getGames(WsContext wsContext) {
        return games.values()
                .stream()
                .filter(g -> g.getPlayers().stream().anyMatch(p -> p.getWsContext().equals(wsContext)))
                .collect(toList());
    }

    @Override
    public void endGame(Game game) {

        games.remove(game.getUuid());

        log.info("Ending game: " + game.getUuid() + ", " + games.size() + " active games");
    }

    @Override
    public GameState getGameState(UUID gameId, WsContext wsContext) {
        final var game = games.get(gameId);
        final var player = game.getPlayerWsContext(wsContext);
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
    public void removePlayerFromRequest(WsContext wsContext) {

        // Remove player from game request, and the request itself if no player is left in request

        this.gameRequests.entrySet()
                .stream()
                .filter(gr -> gr.getValue().getPlayers().stream().anyMatch(p -> p.equals(wsContext)))
                .findFirst()
                .ifPresent(gr -> {

                    gr.getValue().getPlayers().remove(wsContext);
                    if (gr.getValue().getPlayers().size() == 0) {
                        this.gameRequests.remove(gr.getKey());
                    }

                    log.info("Removed " + wsContext.getSessionId() + " from " + gr.getValue());
                });

        log.info(gameRequests.size() + " active game requests");

    }

    @Getter
    @EqualsAndHashCode(of = {"gameUuid"})
    @ToString(of = {"gameUuid"})
    private static class GameRequest {

        private final LocalDateTime creationDate;
        private final UUID gameUuid;
        private final List<WsContext> players = new ArrayList<>();

        GameRequest() {
            this.gameUuid = UUID.randomUUID();
            this.creationDate = LocalDateTime.now();
        }

        void addPlayer(final WsContext wsContext) {
            players.add(wsContext);
        }

    }
}

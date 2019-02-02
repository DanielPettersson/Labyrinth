package se.daniel.labyrinth.service.impl;

import lombok.Getter;
import org.springframework.stereotype.Service;
import se.daniel.labyrinth.model.Game;
import se.daniel.labyrinth.model.GameRequest;
import se.daniel.labyrinth.model.LabyrinthBuilder;
import se.daniel.labyrinth.service.GameService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class GameServiceImpl implements GameService {

    private final Map<UUID, Game> games = new HashMap<>();

    @Getter
    private final List<GameRequest> gameRequests = new ArrayList<>();

    @Override
    public GameRequest createGameRequest() {
        final GameRequest gameRequest = new GameRequest();
        gameRequests.add(gameRequest);
        return gameRequest;
    }

    @Override
    public List<GameRequest> removeTimedOutGameRequests() {
        final LocalDateTime now = LocalDateTime.now();
        final List<GameRequest> oldRequests = gameRequests
                .stream()
                .filter(r -> Duration.between(r.getCreationDate(), now).getSeconds() > 60)
                .collect(toList());

        gameRequests.removeAll(oldRequests);
        return oldRequests;
    }

    @Override
    public Game startGame(String uuid) {
        gameRequests.remove(new GameRequest(UUID.fromString(uuid)));
        return new Game(
                UUID.fromString(uuid),
                new LabyrinthBuilder(new Random()).build(10)
        );
    }

}

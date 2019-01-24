package se.daniel.labyrinth.service.impl;

import org.springframework.stereotype.Service;
import se.daniel.labyrinth.model.Game;
import se.daniel.labyrinth.service.GameService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    @Override
    public Optional<Game> getGame(String id) {
        return Optional.ofNullable(games.get(UUID.fromString(id)));
    }

    @Override
    public String createGame() {
        final UUID uuid = UUID.randomUUID();
        games.put(uuid, new Game());
        return uuid.toString();
    }

}

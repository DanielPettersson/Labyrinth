package se.daniel.labyrinth.service;

import se.daniel.labyrinth.model.Game;

import java.util.Optional;

public interface GameService {

    Optional<Game> getGame(String id);

    String createGame();

}

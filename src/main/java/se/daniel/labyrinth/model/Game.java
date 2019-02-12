package se.daniel.labyrinth.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Game {
    private final UUID uuid;
    private final Labyrinth labyrinth;
    private final List<Player> players;

    public boolean isValidMove(Player player, Location move) {
        final Location from = player.getLocation();
        final Location to = from.add(move);
        return labyrinth.isValidMove(from, to);
    }
}

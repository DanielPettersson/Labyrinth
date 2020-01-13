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

        // Can only move a single step

        if (Math.abs(move.getX() + move.getY()) != 1) {
            return false;
        }

        // Check labyrinth constraints

        else if (!labyrinth.isValidMove(from, to, getPlayers().indexOf(player))) {
            return false;
        }

        // Check collision with other players

        return players.stream().noneMatch(p -> p.getLocation().equals(to));

    }

    public Player getPlayerById(String playerId) {
        return players.stream().filter(p -> playerId.equals(p.getId())).findFirst().orElseThrow();
    }
    
    public void playerQuit(Player player) {
        
        final var playerIndex = getPlayers().indexOf(player);
        
        labyrinth.getCells()
                .stream()
                .flatMap(l -> l.stream())
                .filter(c -> c.getOwnerIndex() == playerIndex)
                .forEach(Cell::disOwn);        
    }

}

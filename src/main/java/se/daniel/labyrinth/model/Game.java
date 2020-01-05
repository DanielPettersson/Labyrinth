package se.daniel.labyrinth.model;


import io.javalin.websocket.WsContext;
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

    public Player getPlayerWsContext(WsContext wsContext) {
        return players.stream().filter(p -> wsContext.equals(p.getWsContext())).findFirst().orElseThrow();
    }

}

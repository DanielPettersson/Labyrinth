package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Move {
    private final String gameId;
    private final String playerId;
    private final Location to;
}

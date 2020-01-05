package se.daniel.labyrinth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Move {
    private String gameId;
    private Location to;
}

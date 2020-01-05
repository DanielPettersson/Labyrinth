package se.daniel.labyrinth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class GameSpecification {
    private int numPlayers;
    private int gameSize;
}

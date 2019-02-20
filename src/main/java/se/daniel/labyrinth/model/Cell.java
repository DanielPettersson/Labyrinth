package se.daniel.labyrinth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cell {

    private boolean visited;
    private int ownerIndex;
    private final boolean[] walls;

    Cell() {
        ownerIndex = -1;
        visited = false;
        walls = new boolean[] {true, true, true, true};
    }


}

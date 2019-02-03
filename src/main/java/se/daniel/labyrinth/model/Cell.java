package se.daniel.labyrinth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Cell {

    private boolean visited;
    private final boolean[] walls;

    Cell() {
        visited = false;
        walls = new boolean[] {true, true, true, true};
    }


}

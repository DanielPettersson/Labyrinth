package se.daniel.labyrinth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode(of = {"uuid"})
class Cell {

    private final UUID uuid;
    private final boolean[] walls;

    Cell() {
        uuid = UUID.randomUUID();
        walls = new boolean[] {true, true, true, true};
    }


}

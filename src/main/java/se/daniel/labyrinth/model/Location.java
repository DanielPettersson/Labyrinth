package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Location {
    private final int x;
    private final int y;

    public Location add(Location add) {
        return new Location(x + add.x, y + add.y);
    }

}

package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Location {
    private int x;
    private int y;

    public Location add(Location add) {
        return new Location(x + add.x, y + add.y);
    }

}

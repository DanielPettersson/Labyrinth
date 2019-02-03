package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
class Location {
    private final int x;
    private final int y;
}

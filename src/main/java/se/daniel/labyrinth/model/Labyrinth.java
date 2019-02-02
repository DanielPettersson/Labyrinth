package se.daniel.labyrinth.model;

import lombok.Getter;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Getter
class Labyrinth {

    private final int size;
    private final List<List<Cell>> cells;

    Labyrinth(final int size) {

        this.size = size;
        cells = range(0, size)
                .mapToObj(
                        y -> range(0, size)
                                .mapToObj(x -> new Cell())
                                .collect(toList())
                )
                .collect(toList());
    }

    Cell getCell(final Location location) {
        return cells.get(location.getY()).get(location.getY());
    }

}

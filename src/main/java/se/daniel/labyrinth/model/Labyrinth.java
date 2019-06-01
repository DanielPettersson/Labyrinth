package se.daniel.labyrinth.model;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Getter
public class Labyrinth {

    private final int size;
    private final List<List<Cell>> cells;

    public Labyrinth(final int size) {

        this.size = size;
        cells = range(0, size)
                .mapToObj(
                        y -> range(0, size)
                                .mapToObj(x -> new Cell())
                                .collect(toList())
                )
                .collect(toList());
    }

    public boolean isAllCellsOwned() {
        return getCells().stream().flatMap(Collection::stream).allMatch(c -> c.getOwnerIndex() != -1);
    }

    public Cell getCell(final Location location) {
        return cells.get(location.getY()).get(location.getX());
    }

    boolean isValidMove(Location from, Location to, int playerIndex) {

        // Do not move outside of labyrinth

        if (to.getX() < 0 || to.getY() < 0 || to.getX() >= size || to.getY() >= size) {
            return false;
        }

        // Do not move through walls

        else if (getCell(from).getWalls()[getDirectionIndex(from, to)]) {
            return false;
        }

        // Do not visit same cell too many times

        else {
            return getCell(to).canVisit(playerIndex);
        }

    }

    private int getDirectionIndex(Location from, Location to) {

        var dx = to.getX() - from.getX();

        if (dx == 1) {
            return 1;
        }

        if (dx == -1) {
            return 3;
        }

        var dy = to.getY() - from.getY();

        if (dy == -1) {
            return 0;
        }

        if (dy == 1) {
            return 2;
        }

        throw new IllegalArgumentException("Invalid move");
    }

}

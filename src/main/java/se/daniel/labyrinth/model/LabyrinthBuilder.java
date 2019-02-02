package se.daniel.labyrinth.model;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LabyrinthBuilder {

    private final Random random;

    private Set<Cell> visitedCells;
    private Labyrinth labyrinth;

    public Labyrinth build(final int size) {

        visitedCells = new HashSet<>();
        labyrinth = new Labyrinth(size);

        final Location startLocation = new Location(random.nextInt(size), random.nextInt(size));
        visitCell(startLocation, startLocation);

        return labyrinth;
    }

    private void visitCell(final Location originLocation, Location location) {

        var originCell = labyrinth.getCell(originLocation);
        var cell = labyrinth.getCell(location);

        visitedCells.add(cell);

        // Break down walls

        if (originLocation.getX() > location.getX()) {
            cell.getWalls()[1] = false;
            originCell.getWalls()[3] = false;
        }
        if (originLocation.getX() < location.getX()) {
            cell.getWalls()[3] = false;
            originCell.getWalls()[1] = false;
        }
        if (originLocation.getY() > location.getY()) {
            cell.getWalls()[2] = false;
            originCell.getWalls()[0] = false;
        }
        if (originLocation.getY() < location.getY()) {
            cell.getWalls()[0] = false;
            originCell.getWalls()[2] = false;
        }

        final List<Location> locationsToVisit = getLocationsToVisit(location);
        locationsToVisit.forEach(l -> visitCell(location, l));
    }

    private List<Location> getLocationsToVisit(final Location location) {

        List<Location> destinations = new ArrayList<>();
        destinations.add(new Location(location.getX(), location.getY() - 1));
        destinations.add(new Location(location.getX() + 1, location.getY()));
        destinations.add(new Location(location.getX(), location.getY() + 1));
        destinations.add(new Location(location.getX() - 1, location.getY()));
        Collections.shuffle(destinations, random);

        final List<Location> filter1 = destinations.stream()
                .filter(
                        l -> l.getX() >= 0 && l.getX() < labyrinth.getSize() && l.getY() >= 0 && l.getY() < labyrinth.getSize()
                ).collect(Collectors.toList());
        final List<Location> filter2 = filter1.stream().filter(
                l -> !visitedCells.contains(labyrinth.getCell(l))
        ).collect(Collectors.toList());

        return filter2;
    }

}

package se.daniel.labyrinth.util;

import lombok.RequiredArgsConstructor;
import se.daniel.labyrinth.model.Cell;
import se.daniel.labyrinth.model.Labyrinth;
import se.daniel.labyrinth.model.Location;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LabyrinthBuilder {

    private final Random random;

    private Set<Location> visitedLocations;
    private Labyrinth labyrinth;

    public Labyrinth build(int size) {

        visitedLocations = new HashSet<>();
        labyrinth = new Labyrinth(size);

        // Create labyrinth

        final Location startLocation = randomLocation(size);
        visitCell(startLocation, startLocation);

        // Break down some more walls

        IntStream.range(0, size * 2).forEach(i -> {
            final var origLocation = randomLocation(size);
            final var locations = getLocationsToVisit(origLocation);
            final var location = locations.get(random.nextInt(locations.size()));
            var origCell = labyrinth.getCell(origLocation);
            var cell = labyrinth.getCell(location);
            breakDownWalls(origLocation, location, origCell, cell);
        });

        return labyrinth;
    }

    private Location randomLocation(final int size) {
        return new Location(random.nextInt(size), random.nextInt(size));
    }

    private void visitCell(Location originLocation, Location location) {

        // Already visited, abort

        if (visitedLocations.contains(location)) {
            return;
        }

        var originCell = labyrinth.getCell(originLocation);
        var cell = labyrinth.getCell(location);

        visitedLocations.add(location);

        breakDownWalls(originLocation, location, originCell, cell);

        final List<Location> locationsToVisit = getLocationsToVisit(location);
        locationsToVisit.forEach(l -> visitCell(location, l));
    }

    private void breakDownWalls(Location originLocation, Location location, Cell originCell, Cell cell) {
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
    }

    private List<Location> getLocationsToVisit(Location location) {

        List<Location> destinations = new ArrayList<>();
        destinations.add(new Location(location.getX(), location.getY() - 1));
        destinations.add(new Location(location.getX() + 1, location.getY()));
        destinations.add(new Location(location.getX(), location.getY() + 1));
        destinations.add(new Location(location.getX() - 1, location.getY()));
        Collections.shuffle(destinations, random);

        return destinations
                .stream()
                .filter(
                        l -> l.getX() >= 0 && l.getX() < labyrinth.getSize() && l.getY() >= 0 && l.getY() < labyrinth.getSize()
                )
                .collect(toList());

    }

}

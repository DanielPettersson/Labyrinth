package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.IntStream;

@Getter
@AllArgsConstructor
public class GameEnded {
    private final List<Player> players;
    private final List<Integer> points;

    public static GameEnded fromGame(Game game) {

        final List<Integer> points = new ArrayList<>();
        final SortedMap<Integer, Integer> cellOwners = new TreeMap<>();

        game.getLabyrinth().getCells().stream().flatMap(Collection::stream).mapToInt(Cell::getOwnerIndex).forEach(
                ownerIndex -> {
                    cellOwners.putIfAbsent(ownerIndex, 0);
                    cellOwners.put(ownerIndex, cellOwners.get(ownerIndex) + 1);
                }
        );

        IntStream.range(0, game.getPlayers().size()).forEach(i -> points.add(cellOwners.getOrDefault(i, 0)));

        return new GameEnded(game.getPlayers(), points);
    }
}

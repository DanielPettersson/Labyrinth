package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GameState {
    private final List<List<Integer>> cellsOwnerIndices;
    private final List<Player> players;
}

package se.daniel.labyrinth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Cell {

    private int ownerIndex;
    private final Boolean[] walls;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final Map<Integer, Integer> visitsMap;

    Cell() {
        ownerIndex = -1;
        walls = new Boolean[] {true, true, true, true};
        visitsMap = new HashMap<>();
    }

    public void playerVisit(final int playerIndex) {
        visitsMap.putIfAbsent(playerIndex, 0);
        visitsMap.put(playerIndex, visitsMap.get(playerIndex) + 1);
        ownerIndex = playerIndex;
    }

    public void disOwn() {
        ownerIndex = -1;
    }
    
    public boolean canVisit(final int playerIndex) {
        return 4 - numWalls() > visitsMap.getOrDefault(playerIndex, 0);
    }

    private long numWalls() {
        return Arrays.stream(walls).filter(w -> w).count();
    }
}

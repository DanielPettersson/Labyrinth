package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinInfo {
    private final int playerIndex;
    private final Game game;
}

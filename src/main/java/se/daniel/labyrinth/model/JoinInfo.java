package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JoinInfo {

    private final UUID gameUuid;
    private final int playerIndex;

    private final Game game;
}

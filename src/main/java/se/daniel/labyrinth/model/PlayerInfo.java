package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayerInfo {

    private final UUID gameUuid;
    private final UUID playerUuid;
    private final int playerIndex;
}

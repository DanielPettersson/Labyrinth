package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JoinGameRequest {
    private final UUID playerUuid;
}

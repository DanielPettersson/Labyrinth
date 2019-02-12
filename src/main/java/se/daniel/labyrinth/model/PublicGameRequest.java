package se.daniel.labyrinth.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PublicGameRequest {

    private final UUID uuid;

    public PublicGameRequest(final GameRequest gameRequest) {
        this.uuid = gameRequest.getGameUuid();
    }
}

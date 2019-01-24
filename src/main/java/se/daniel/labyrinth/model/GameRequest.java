package se.daniel.labyrinth.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = {"uuid"})
public class GameRequest {

    LocalDateTime creationDate;
    UUID uuid;

    public GameRequest() {
        this(UUID.randomUUID());
    }

    public GameRequest(UUID uuid) {
        this.uuid = uuid;
        this.creationDate = LocalDateTime.now();
    }
}

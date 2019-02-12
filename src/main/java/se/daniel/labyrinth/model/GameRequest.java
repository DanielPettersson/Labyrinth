package se.daniel.labyrinth.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = {"gameUuid"})
public class GameRequest {

    private final LocalDateTime creationDate;
    private final UUID gameUuid;
    private final List<UUID> playerUuids = new ArrayList<>();

    public GameRequest() {
        this.gameUuid = UUID.randomUUID();
        this.playerUuids.add(UUID.randomUUID());
        this.creationDate = LocalDateTime.now();
    }

    public UUID addPlayer() {
        final UUID uuid = UUID.randomUUID();
        playerUuids.add(uuid);
        return uuid;
    }

}

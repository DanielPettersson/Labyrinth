package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Player {

    private final UUID uuid;
    private Location location;
}

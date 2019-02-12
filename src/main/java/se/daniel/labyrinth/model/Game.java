package se.daniel.labyrinth.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Game {
    private final UUID uuid;
    private final Labyrinth labyrinth;
}

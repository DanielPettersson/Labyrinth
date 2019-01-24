package se.daniel.labyrinth.model;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Game {
    private final List<Message> messages = new ArrayList<>();
}

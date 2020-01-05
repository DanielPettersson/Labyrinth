package se.daniel.labyrinth.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Command<T> {

    private final String command;
    private final T content;

}

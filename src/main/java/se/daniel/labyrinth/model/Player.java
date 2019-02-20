package se.daniel.labyrinth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Player {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final UUID uuid;

    private Location location;
}

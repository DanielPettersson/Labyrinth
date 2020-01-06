package se.daniel.labyrinth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Player {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final String id;

    private Location location;
}

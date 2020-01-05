package se.daniel.labyrinth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assert {

    public static void isTrue(final boolean statement, final String message) {
        if (!statement) {
            throw new IllegalArgumentException(message);
        }
    }

}

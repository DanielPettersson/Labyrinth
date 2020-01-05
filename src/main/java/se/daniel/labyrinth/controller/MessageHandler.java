package se.daniel.labyrinth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.function.BiConsumer;

@AllArgsConstructor
public class MessageHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, BiConsumer<ObjectMapper, String>> messageMap;

    public void dispatch(final String message) {
        try {
            final var root = mapper.readValue(message, Map.class);

            final String command = (String) root.get("command");
            final String content = (String) root.get("content");

            if (messageMap.containsKey(command)) {

                messageMap.get(command).accept(mapper, content);

            } else {
                throw new IllegalStateException("unknown command: " + command);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface MessageConsumer {

        void accept(ObjectMapper mapper, String mesage) throws JsonProcessingException;

        static BiConsumer<ObjectMapper, String> toMessageConsumer(final MessageConsumer messageConsumer) {
            return (mapper, message) -> {
                try {
                    messageConsumer.accept(mapper, message);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            };
        }

    }

}

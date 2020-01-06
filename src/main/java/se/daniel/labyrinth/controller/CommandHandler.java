package se.daniel.labyrinth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class CommandHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, MessageConsumer> messageMap = new HashMap<>();

    public void addDispatchCommand(final String command, final MessageConsumer consumer) {
        messageMap.put(command, consumer);
    }

    public void dispatch(final WsMessageContext wsContext) {
        try {
            final var root = mapper.readTree(wsContext.message());

            final String command = root.get("command").asText();
            final String content = root.get("content").toString();

            if (messageMap.containsKey(command)) {
                messageMap.get(command).accept(mapper, content, wsContext);

            } else {
                throw new IllegalStateException("unknown command: " + command);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void sendCommand(WsContext wsContext, final String command, final T content) {
        wsContext.send(new Command<>(command, content));
    }

    @FunctionalInterface
    public interface MessageConsumer {

        void accept(ObjectMapper mapper, String mesage, WsContext context) throws JsonProcessingException;

    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Command<T> {

        private final String command;
        private final T content;

    }
}

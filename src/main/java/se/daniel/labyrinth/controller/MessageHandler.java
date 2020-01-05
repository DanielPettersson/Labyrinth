package se.daniel.labyrinth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class MessageHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, MessageConsumer> messageMap;

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

    @FunctionalInterface
    public interface MessageConsumer {

        void accept(ObjectMapper mapper, String mesage, WsContext context) throws JsonProcessingException;

    }

}

package se.daniel.labyrinth;

import io.javalin.Javalin;
import se.daniel.labyrinth.controller.GameController;
import se.daniel.labyrinth.controller.MessageHandler;
import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.Move;
import se.daniel.labyrinth.service.impl.GameServiceImpl;

import java.util.Map;

public class Application {

    public static void main(String[] args) {

        final var gameService = new GameServiceImpl();
        final var controller = new GameController(gameService);
        final var messageHandler = new MessageHandler(Map.of(
                "join",
                (mapper, message, wsContext) -> {
                    final var gameSpecification = mapper.readValue(message, GameSpecification.class);
                    controller.joinGameRequest(gameSpecification, wsContext);
                },
                "move",
                (mapper, message, wsContext) -> {
                    final var move = mapper.readValue(message, Move.class);
                    controller.movePlayer(move, wsContext);
                })
        );

        Javalin.create(config -> {
                    config.addStaticFiles("static");
                    config.wsLogger(ws -> {
                        ws.onConnect(ctx -> System.out.println("Connected: " + ctx.getSessionId()));
                        ws.onClose(ctx -> System.out.println("Closed: " + ctx.getSessionId()));
                        ws.onError(ctx -> System.out.println("Error: " + ctx.error()));
                    });
                })
                .ws("ws", ws -> {
                    ws.onMessage(messageHandler::dispatch);
                    ws.onClose(controller::playerDisconnected);
                })
                .start(8080);

    }

}

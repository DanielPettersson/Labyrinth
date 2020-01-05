package se.daniel.labyrinth;

import io.javalin.Javalin;
import se.daniel.labyrinth.controller.GameController;
import se.daniel.labyrinth.controller.MessageHandler;
import se.daniel.labyrinth.model.GameSpecification;
import se.daniel.labyrinth.model.Move;
import se.daniel.labyrinth.service.impl.GameServiceImpl;

import java.util.Map;

import static se.daniel.labyrinth.controller.MessageHandler.MessageConsumer.toMessageConsumer;

public class Application {

    public static void main(String[] args) {

        final var gameService = new GameServiceImpl();
        final var controller = new GameController(gameService);
        final var messageHandler = new MessageHandler(Map.of(
                "join",
                toMessageConsumer((mapper, message) -> {
                    final var gameSpecification = mapper.readValue(message, GameSpecification.class);
                    controller.joinGameRequest(gameSpecification);
                }),
                "move",
                toMessageConsumer((mapper, message) -> {
                    final var move = mapper.readValue(message, Move.class);
                    controller.movePlayer(move);
                })
        ));

        Javalin.create(config -> {
                    config.addStaticFiles("static");
                    config.wsLogger(ws -> {
                        ws.onConnect(ctx -> System.out.println("Connected: " + ctx.getSessionId()));
                        ws.onClose(ctx -> System.out.println("Closed: " + ctx.getSessionId()));
                        ws.onMessage(ctx -> System.out.println("Received: " + ctx.message()));
                        ws.onError(ctx -> System.out.println("Error: " + ctx.error()));
                    });
                })
                .ws("ws", ws -> {
                    ws.onMessage(ctx -> {
                        messageHandler.dispatch(ctx.message());
                    });
                    ws.onClose(ctx -> {

                    });
                    ws.onError(ctx -> {

                    });
                })
                .start(8080);

    }

}

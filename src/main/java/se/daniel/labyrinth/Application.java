package se.daniel.labyrinth;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import se.daniel.labyrinth.controller.CommandHandler;
import se.daniel.labyrinth.controller.LabyrinthController;
import se.daniel.labyrinth.service.impl.LabyrinthEngineImpl;

@Slf4j
public class Application {

    public static void main(String[] args) {

        final var labyrinthEngine = new LabyrinthEngineImpl();
        final var commandHandler = new CommandHandler();
        final var controller = new LabyrinthController(labyrinthEngine, commandHandler);

        Javalin.create(config -> {
                    config.addStaticFiles("static");
                    config.wsLogger(ws -> {
                        ws.onConnect(ctx -> log.debug("Connected: " + ctx.getSessionId()));
                        ws.onClose(ctx -> log.debug("Closed: " + ctx.getSessionId()));
                        ws.onError(ctx -> log.error("Error: " + ctx.error()));
                    });
                })
                .ws("ws", ws -> {
                    ws.onConnect(controller::playerConnected);
                    ws.onMessage(commandHandler::dispatch);
                    ws.onClose(controller::playerDisconnected);
                })
                .wsException(IllegalArgumentException.class, ((exception, ctx) -> commandHandler.sendCommand(ctx, "error", exception.getMessage())))
                .start(8080);

    }

}

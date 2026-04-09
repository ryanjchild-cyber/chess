package server;
import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import service.exceptions.*;
import service.services.GameplayService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final GameplayService gameplayService;
    public WebSocketHandler(GameplayService gameplayService) {
        this.gameplayService = gameplayService;
    }
    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand base = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (base.getCommandType()) {
                case CONNECT -> gameplayService.connect(base.getAuthToken(), base.getGameID(), ctx);
                case LEAVE -> gameplayService.leave(base.getAuthToken(), base.getGameID(), ctx);
                case RESIGN -> gameplayService.resign(base.getAuthToken(), base.getGameID(), ctx);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    gameplayService.makeMove(
                            moveCommand.getAuthToken(),
                            moveCommand.getGameID(),
                            moveCommand.getMove(),
                            ctx
                    );
                }
            }
        } catch (UnauthorizedException | ForbiddenException | BadRequestException ex) {
            sendError(ctx, ex.getMessage());
        } catch (Exception ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }
    public void onClose(WsCloseContext ctx) {
        gameplayService.disconnect(ctx);
    }
    //public void onError(WsErrorContext ctx) {
        // optional
    //}
    private void sendError(WsMessageContext ctx, String message) {
        if (message == null || !message.toLowerCase().contains("error")) {
            message = "Error: " + message;
        }
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }
}
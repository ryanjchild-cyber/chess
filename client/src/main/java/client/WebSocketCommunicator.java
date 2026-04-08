package client;
import com.google.gson.Gson;
import jakarta.websocket.*;
import model.GameData;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import chess.ChessMove;

import java.net.URI;
@ClientEndpoint
public class WebSocketCommunicator {
    private final Gson gson = new Gson();
    private final NotificationHandler handler;
    private Session session;
    public WebSocketCommunicator(String httpUrl, NotificationHandler handler) throws Exception {
        this.handler = handler;
        String wsUrl = httpUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, URI.create(wsUrl));
    }
    @OnMessage
    public void onMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage load = gson.fromJson(message, LoadGameMessage.class);
                handler.loadGame(load.getGame());
            }
            case NOTIFICATION -> {
                NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                handler.notifyMessage(notification.getMessage());
            }
            case ERROR -> {
                ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                handler.errorMessage(error.getErrorMessage());
            }
        }
    }
    public void connect(String authToken, int gameID) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(new ConnectCommand(authToken, gameID)));
    }
    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(new MakeMoveCommand(authToken, gameID, move)));
    }
    public void leave(String authToken, int gameID) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(new LeaveCommand(authToken, gameID)));
    }
    public void resign(String authToken, int gameID) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(new ResignCommand(authToken, gameID)));
    }
    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
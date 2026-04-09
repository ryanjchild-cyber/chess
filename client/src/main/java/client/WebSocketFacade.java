package client;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import javax.websocket.*;
import java.net.URI;
public class WebSocketFacade extends Endpoint {
    public interface Listener {
        void onMessage(String message);
        void onError(String error);
    }
    private final Session session;
    private final Gson gson = new Gson();
    private final Listener listener;
    public WebSocketFacade(String serverUrl, Listener listener) throws Exception {
        this.listener = listener;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        this.session = container.connectToServer(this, uri);
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        session.addMessageHandler(String.class, message -> listener.onMessage(message));
    }
    public void connect(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }
    public void leave(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }
    public void resign(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }
    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        send(new MakeMoveCommand(authToken, gameID, move));
    }
    public void close() throws Exception {
        session.close();
    }
    private void send(Object command) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(command));
    }
}
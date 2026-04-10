package client;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import java.net.URI;
public class WebSocketFacade extends Endpoint {
    public interface Listener {
        void onMessage(String message);
    }
    private Session session;
    private final Gson gson = new Gson();
    private final Listener listener;
    private final String serverUrl;
    public WebSocketFacade(String serverUrl, Listener listener) throws Exception {
        this.serverUrl = serverUrl;
        this.listener = listener;
        connectSession();
    }
    private void connectSession() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // Prevent the client-side websocket from being closed just because it sits idle.
        container.setDefaultMaxSessionIdleTimeout(0);

        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        this.session = container.connectToServer(this, uri);
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        session.addMessageHandler(String.class, message -> listener.onMessage(message));
    }
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
    }
    @Override
    public void onError(Session session, Throwable thr) {
        this.session = null;
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
        if (session != null && session.isOpen()) {
            session.close();
        }
        session = null;
    }
    public boolean isOpen() {
        return session != null && session.isOpen();
    }
    private void ensureOpen() throws Exception {
        if (!isOpen()) {
            connectSession();
        }
    }
    private void send(Object command) throws Exception {
        ensureOpen();
        session.getBasicRemote().sendText(gson.toJson(command));
    }
}
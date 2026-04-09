package server;
import io.javalin.websocket.WsContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class ConnectionManager {
    public enum Role {
        WHITE,
        BLACK,
        OBSERVER
    }
    public static class Connection {
        private final WsContext session;
        private final int gameID;
        private final String username;
        private final Role role;
        public Connection(WsContext session, int gameID, String username, Role role) {
            this.session = session;
            this.gameID = gameID;
            this.username = username;
            this.role = role;
        }
        public WsContext getSession() {
            return session;
        }
        public int getGameID() {
            return gameID;
        }
        public String getUsername() {
            return username;
        }
        public Role getRole() {
            return role;
        }
    }
    private final ConcurrentHashMap<Integer, Set<Connection>> connections = new ConcurrentHashMap<>();
    public void add(WsContext session, int gameID, String username, Role role) {
        Set<Connection> gameConnections =
                connections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet());
        gameConnections.removeIf(connection ->
                connection.getUsername().equals(username) ||
                        connection.getSession().sessionId().equals(session.sessionId())
        );
        gameConnections.add(new Connection(session, gameID, username, role));
    }
    public void remove(WsContext session) {
        for (var entry : connections.entrySet()) {
            Set<Connection> gameConnections = entry.getValue();
            gameConnections.removeIf(connection ->
                    connection.getSession().sessionId().equals(session.sessionId())
            );
            if (gameConnections.isEmpty()) {
                connections.remove(entry.getKey(), gameConnections);
            }
        }
    }
    public Set<Connection> getGameConnections(int gameID) {
        return connections.getOrDefault(gameID, ConcurrentHashMap.newKeySet());
    }
}
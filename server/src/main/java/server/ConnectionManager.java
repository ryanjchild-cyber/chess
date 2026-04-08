package server;

import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public enum Role {
        WHITE, BLACK, OBSERVER
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

    private final Map<Integer, Set<Connection>> gameConnections = new ConcurrentHashMap<>();
    private final Map<String, Connection> sessionConnections = new ConcurrentHashMap<>();

    public void add(WsContext session, int gameID, String username, Role role) {
        Connection connection = new Connection(session, gameID, username, role);
        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(connection);
        sessionConnections.put(session.getSessionId(), connection);
    }

    public void remove(WsContext session) {
        Connection connection = sessionConnections.remove(session.getSessionId());
        if (connection == null) {
            return;
        }

        Set<Connection> connections = gameConnections.get(connection.getGameID());
        if (connections != null) {
            connections.removeIf(c -> c.getSession().getSessionId().equals(session.getSessionId()));
            if (connections.isEmpty()) {
                gameConnections.remove(connection.getGameID());
            }
        }
    }

    public Set<Connection> getGameConnections(int gameID) {
        return gameConnections.getOrDefault(gameID, Set.of());
    }
}
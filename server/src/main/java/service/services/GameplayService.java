package service.services;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import server.ConnectionManager;
import service.exceptions.*;
import websocket.messages.*;
public class GameplayService {
    private final DataAccess dao;
    private final ConnectionManager connections;
    private final Gson gson = new Gson();
    public GameplayService(DataAccess dao, ConnectionManager connections) {
        this.dao = dao;
        this.connections = connections;
    }
    public void connect(String authToken, Integer gameID, WsContext session) throws DataAccessException {
        AuthData auth = requireAuth(authToken);
        GameData game = requireGame(gameID);
        ConnectionManager.Role role = roleForUser(auth.username(), game);
        connections.add(session, gameID, auth.username(), role);
        send(session, new LoadGameMessage(game));
        String msg = switch (role) {
            case WHITE -> auth.username() + " connected as white";
            case BLACK -> auth.username() + " connected as black";
            case OBSERVER -> auth.username() + " connected as an observer";
        };
        notifyOthers(gameID, auth.username(), msg);
    }
    public void makeMove(String authToken, Integer gameID, ChessMove move, WsContext session) throws DataAccessException {
        AuthData auth = requireAuth(authToken);
        GameData gameData = requireGame(gameID);
        ConnectionManager.Role role = roleForUser(auth.username(), gameData);
        if (gameData.gameOver()) {
            throw new ForbiddenException("Error: game already over");
        }
        if (role == ConnectionManager.Role.OBSERVER) {
            throw new ForbiddenException("Error: observers cannot make moves");
        }
        ChessGame game = gameData.game();
        if (role == ConnectionManager.Role.WHITE && game.getTeamTurn() != ChessGame.TeamColor.WHITE) {
            throw new ForbiddenException("Error: not your turn");
        }
        if (role == ConnectionManager.Role.BLACK && game.getTeamTurn() != ChessGame.TeamColor.BLACK) {
            throw new ForbiddenException("Error: not your turn");
        }
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new BadRequestException("Error: invalid move");
        }
        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game,
                gameData.gameOver()
        );
        dao.updateGame(updatedGame);
        send(session, new LoadGameMessage(updatedGame));
        broadcastGameOthers(gameID, auth.username(), updatedGame);
        notifyOthers(gameID, auth.username(), auth.username() + " made move " + moveToString(move));        if (game.isInCheck(game.getTeamTurn()) && !game.isInCheckmate(game.getTeamTurn())) {
            String checkedPlayer = game.getTeamTurn() == ChessGame.TeamColor.WHITE
                    ? gameData.whiteUsername()
                    : gameData.blackUsername();
            broadcastNotification(gameID, checkedPlayer + " is in check");
        }
        if (game.isInCheckmate(game.getTeamTurn())) {
            String matedPlayer = game.getTeamTurn() == ChessGame.TeamColor.WHITE
                    ? gameData.whiteUsername()
                    : gameData.blackUsername();
            broadcastNotification(gameID, matedPlayer + " is in checkmate");
        }
        if (game.isInStalemate(game.getTeamTurn())) {
            broadcastNotification(gameID, "The game is in stalemate");
        }
    }
    public void leave(String authToken, Integer gameID, WsContext session) throws DataAccessException {
        AuthData auth = requireAuth(authToken);
        GameData game = requireGame(gameID);
        ConnectionManager.Role role = roleForUser(auth.username(), game);
        connections.remove(session);
        if (role == ConnectionManager.Role.WHITE) {
            dao.updateGame(new GameData(
                    game.gameID(),
                    null,
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            ));
        } else if (role == ConnectionManager.Role.BLACK) {
            dao.updateGame(new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    null,
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            ));
        }
        notifyOthers(gameID, auth.username(), auth.username() + " left the game");
        try {
            session.session.close();
        } catch (Exception ignored) {
        }
    }
    public void resign(String authToken, Integer gameID, WsContext session) throws DataAccessException {
        AuthData auth = requireAuth(authToken);
        GameData game = requireGame(gameID);
        ConnectionManager.Role role = roleForUser(auth.username(), game);
        if (role == ConnectionManager.Role.OBSERVER) {
            throw new ForbiddenException("Error: observers cannot resign");
        }
        if (game.gameOver()) {
            throw new ForbiddenException("Error: game already over");
        }
        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                true
        );
        dao.updateGame(updatedGame);
        broadcastNotification(gameID, auth.username() + " resigned the game");
    }
    private AuthData requireAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        AuthData auth;
        try {
            auth = dao.getAuth(authToken);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth;
    }
    private GameData requireGame(Integer gameID) throws DataAccessException {
        if (gameID == null || gameID <= 0) {
            throw new BadRequestException("Error: bad game id");
        }
        GameData game = dao.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("Error: game not found");
        }
        return game;
    }
    private ConnectionManager.Role roleForUser(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return ConnectionManager.Role.WHITE;
        }
        if (username.equals(game.blackUsername())) {
            return ConnectionManager.Role.BLACK;
        }
        return ConnectionManager.Role.OBSERVER;
    }
    private void broadcastNotification(int gameID, String text) {
        NotificationMessage message = new NotificationMessage(text);
        for (var connection : connections.getGameConnections(gameID)) {
            send(connection.getSession(), message);
        }
    }
    private void notifyOthers(int gameID, String excludeUsername, String text) {
        NotificationMessage message = new NotificationMessage(text);
        for (var connection : connections.getGameConnections(gameID)) {
            if (!connection.getUsername().equals(excludeUsername)) {
                send(connection.getSession(), message);
            }
        }
    }
    private void send(WsContext session, Object message) {
        try {
            session.send(gson.toJson(message));
        } catch (Exception e) {
            connections.remove(session);
        }
    }
    private String moveToString(ChessMove move) {
        return posToString(move.getStartPosition()) + "-" + posToString(move.getEndPosition());
    }
    private String posToString(ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1);
        return "" + file + pos.getRow();
    }
    public void disconnect(WsContext session) {
        connections.remove(session);
    }
    private void broadcastGameOthers(int gameID, String excludeUsername, GameData game) {
        LoadGameMessage message = new LoadGameMessage(game);
        for (var connection : connections.getGameConnections(gameID)) {
            if (!connection.getUsername().equals(excludeUsername)) {
                send(connection.getSession(), message);
            }
        }
    }
}
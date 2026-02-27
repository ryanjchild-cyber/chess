package service;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;

public class GameService {
    private final DataAccess dao;

    public GameService(DataAccess dao) {
        this.dao=dao;
    }
    public ListGamesResult listGames(String authToken) throws DataAccessException {
        requireAuth(authToken);
        var games=new ArrayList<GameSummary>();
        for (GameData game:dao.listGames()) {
            games.add(new GameSummary(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        return new ListGamesResult(games);
    }
    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        requireAuth(authToken);
        if (request==null||request.gameName()==null||request.gameName().isBlank()) {
            throw new BadRequestException();
        }
        ChessGame game=new ChessGame();
        int id=dao.createGame(new GameData(0,null,null,request.gameName(),game));
        return new CreateGameResult(id);
    }
    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        AuthData auth=requireAuth(authToken);
        if (request==null||request.gameID()<=0||request.playerColor()==null) {
            throw new BadRequestException();
        }
        GameData game=dao.getGame(request.gameID());
        if(game==null) {
            throw new BadRequestException();
        }
        String color=request.playerColor().trim().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new BadRequestException();
        }
        if (color.equals("WHITE")) {
            if (game.whiteUsername()!=null) throw new ForbiddenException();
            dao.updateGame(new GameData(game.gameID(),auth.username(),game.blackUsername(),game.gameName(),game.game()));
        } else {
            if (game.blackUsername()!=null) throw new ForbiddenException();
            dao.updateGame(new GameData(game.gameID(),game.whiteUsername(),auth.username(),game.gameName(),game.game()));
        }
    }
    private AuthData requireAuth(String authToken) throws DataAccessException {
        if (authToken==null||authToken.isBlank()) throw new UnauthorizedException();
        AuthData auth=dao.getAuth(authToken);
        if (auth==null) throw new UnauthorizedException();
        return auth;
    }
}

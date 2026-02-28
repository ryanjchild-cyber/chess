package dataaccess;
import model.UserData;
import model.AuthData;
import model.GameData;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users=new ConcurrentHashMap<>();
    private final Map<String, AuthData> auths=new ConcurrentHashMap<>();
    private final Map<Integer, GameData> games=new ConcurrentHashMap<>();
    private final AtomicInteger nextGameID=new AtomicInteger(1);
    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameID.set(1);
    }
    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user==null) {throw new DataAccessException("User is null");}
        if (users.putIfAbsent(user.username(),user)!=null) {
            throw new DataAccessException("already taken");
        }
    }
    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth==null) {throw new DataAccessException("auth is null");}
        auths.put(auth.authToken(), auth);
    }
    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }
    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken==null||authToken.isBlank()) {
            throw new DataAccessException("unauthorized");
        }
        if (auths.remove(authToken)==null) {
            throw new DataAccessException("unauthorized");
        }
    }
    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game==null) {throw new DataAccessException("game is null");}
        int id = nextGameID.getAndIncrement();
        games.put(id, new GameData(id,game.whiteUsername(),game.blackUsername(),game.gameName(),game.game()));
        return id;
    }
    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }
    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {throw new DataAccessException("game is null");}
        if (!games.containsKey(game.gameID())) {throw new DataAccessException("game not found");}
        games.put(game.gameID(), game);
    }
}

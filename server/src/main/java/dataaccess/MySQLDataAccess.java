package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
public class MySQLDataAccess implements DataAccess {
    private final Gson gson=new Gson();
    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }
    private void configureDatabase() throws DataAccessException {
        try {
            DatabaseManager.createDatabase();
            try (var connection=DatabaseManager.getConnection()) {
                try (var statement=connection.createStatement()) {
                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS user (
                            username VARCHAR(255) NOT NULL PRIMARY KEY,
                            password VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL
                        )
                    """);
                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS auth (
                            authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                            username VARCHAR(255) NOT NULL
                        )
                    """);
                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS game (
                            gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            whiteUsername VARCHAR(255),
                            blackUsername VARCHAR(255),
                            gameName VARCHAR(255) NOT NULL,
                            gameJson TEXT NOT NULL,
                            gameOver BOOLEAN NOT NULL DEFAULT FALSE
                        )
                    """);
                    statement.executeUpdate("""
                        ALTER TABLE game
                        ADD COLUMN gameOver BOOLEAN NOT NULL DEFAULT FALSE
                    """);
                } catch (Exception ignored) {
                    //column already exists
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to configure database");
        }
    }
    @Override
    public void clear() throws DataAccessException {
        try (var connection=DatabaseManager.getConnection()) {
            try (var statement=connection.createStatement()) {
                statement.executeUpdate("DELETE FROM auth");
                statement.executeUpdate("DELETE FROM game");
                statement.executeUpdate("DELETE FROM user");
                statement.executeUpdate("ALTER TABLE game AUTO_INCREMENT = 1");
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear database");
        }
    }
    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user==null) {
            throw new DataAccessException("User is null");
        }
        if (getUser(user.username())!=null) {
            throw new DataAccessException("already taken");
        }
        String hashedPassword=BCrypt.hashpw(user.password(),BCrypt.gensalt());
        try (var connection=DatabaseManager.getConnection();
             PreparedStatement statement= connection.prepareStatement(
                     "INSERT INTO user (username, password, email) VALUES (?, ?, ?)")) {
            statement.setString(1,user.username());
            statement.setString(2,hashedPassword);
            statement.setString(3,user.email());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Unable to create user");
        }
    }
    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT username, password, email FROM user WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new UserData(
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("email"));
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Unable to get user");
        }
    }
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("auth is null");
        }
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO auth (authToken, username) VALUES (?, ?)")) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Unable to create auth");
        }
    }
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT authToken, username FROM auth WHERE authToken = ?")) {
            statement.setString(1, authToken);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new AuthData(
                            result.getString("authToken"),
                            result.getString("username"));
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Unable to get auth");
        }
    }
    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException("unauthorized");
        }
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM auth WHERE authToken = ?")) {
            statement.setString(1, authToken);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("unauthorized");
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Unable to delete auth");
        }
    }
    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game is null");
        }
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO game (whiteUsername, blackUsername, gameName, gameJson, gameOver) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setBoolean(5, game.gameOver());
            statement.executeUpdate();
            try (ResultSet result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
            throw new DataAccessException("Unable to get game ID");
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Unable to create game");
        }
    }
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson, gameOver FROM game WHERE gameID = ?")) {
            statement.setInt(1, gameID);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    ChessGame game = gson.fromJson(result.getString("gameJson"), ChessGame.class);
                    return new GameData(
                            result.getInt("gameID"),
                            result.getString("whiteUsername"),
                            result.getString("blackUsername"),
                            result.getString("gameName"),
                            game,
                            result.getBoolean("gameOver")
                    );
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Unable to get game");
        }
    }
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson, gameOver FROM game")) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ChessGame game = gson.fromJson(result.getString("gameJson"), ChessGame.class);
                    games.add(new GameData(
                            result.getInt("gameID"),
                            result.getString("whiteUsername"),
                            result.getString("blackUsername"),
                            result.getString("gameName"),
                            game,
                            result.getBoolean("gameOver")
                    ));
                }
            }
            return games;
        } catch (Exception e) {
            throw new DataAccessException("Unable to list games");
        }
    }
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game is null");
        }
        try (var connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameJson = ?, gameOver = ? WHERE gameID = ?")) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setBoolean(5, game.gameOver());
            statement.setInt(6, game.gameID());
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("game not found");
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Unable to update game");
        }
    }
    @Override
    public boolean verifyUser(String username, String clearTextPassword) throws DataAccessException {
        UserData user=getUser(username);
        if (user==null) {
            return false;
        }
        return BCrypt.checkpw(clearTextPassword,user.password());
    }
}
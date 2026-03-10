package dataaccess;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
public class MySQLDataAccessTests {
    private MySQLDataAccess dao;
    @BeforeEach
    void setUp() throws DataAccessException {
        dao=new MySQLDataAccess();
        dao.clear();
    }
    private UserData makeUser() {
        return new UserData("jim","password123","jim@email.com");
    }
    private AuthData makeAuth() {
        return new AuthData("token123","jim");
    }
    private GameData makeGame(String name) {
        return new GameData(0,null,null,name,new ChessGame());
    }
    @Test
    void clearPositive() throws DataAccessException {
        dao.createUser(makeUser());
        dao.createAuth(makeAuth());
        int gameID=dao.createGame(makeGame("game1"));
        dao.clear();
        assertNull(dao.getUser("jim"));
        assertNull(dao.getAuth("token123"));
        assertNull(dao.getGame(gameID));
        assertTrue(dao.listGames().isEmpty());
    }
    @Test
    void createUserPositive() throws DataAccessException {
        UserData user=makeUser();
        dao.createUser(user);
        UserData stored=dao.getUser("jim");
        assertNotNull(stored);
        assertEquals("jim",stored.username());
        assertEquals("jim@email.com",stored.email());
        assertNotEquals("password123",stored.password());
        assertTrue(dao.verifyUser("jim","password123"));
    }
    @Test
    void createUserNegativeDuplicate() throws DataAccessException {
        dao.createUser(makeUser());
        assertThrows(DataAccessException.class,()->dao.createUser(makeUser()));
    }
    @Test
    void getUserPositive() throws DataAccessException {
        dao.createUser(makeUser());
        UserData stored=dao.getUser("jim");
        assertNotNull(stored);
        assertEquals("jim",stored.username());
        assertEquals("jim@email.com",stored.email());
    }
    @Test
    void getUserNegativeMissing() throws DataAccessException {
        UserData stored=dao.getUser("missing");
        assertNull(stored);
    }
    @Test
    void verifyUserPositive() throws DataAccessException {
        dao.createUser(makeUser());
        assertTrue(dao.verifyUser("jim", "password123"));
    }
    @Test
    void verifyUserNegativeWrongPassword() throws DataAccessException {
        dao.createUser(makeUser());
        assertFalse(dao.verifyUser("jim", "wrongPassword"));
    }
    @Test
    void createAuthPositive() throws DataAccessException {
        dao.createUser(makeUser());
        AuthData auth=makeAuth();
        dao.createAuth(auth);
        AuthData stored=dao.getAuth("token123");
        assertNotNull(stored);
        assertEquals("token123", stored.authToken());
        assertEquals("jim", stored.username());
    }
    @Test
    void createAuthNegativeNull() {
        assertThrows(DataAccessException.class, () -> dao.createAuth(null));
    }
    @Test
    void getAuthPositive() throws DataAccessException {
        dao.createUser(makeUser());
        dao.createAuth(makeAuth());
        AuthData stored=dao.getAuth("token123");
        assertNotNull(stored);
        assertEquals("token123", stored.authToken());
        assertEquals("jim", stored.username());
    }
    @Test
    void getAuthNegativeMissing() throws DataAccessException {
        AuthData stored=dao.getAuth("missingToken");
        assertNull(stored);
    }
    @Test
    void deleteAuthPositive() throws DataAccessException {
        dao.createUser(makeUser());
        dao.createAuth(makeAuth());
        dao.deleteAuth("token123");
        assertNull(dao.getAuth("token123"));
    }
    @Test
    void deleteAuthNegativeMissing() {
        assertThrows(DataAccessException.class, () -> dao.deleteAuth("missingToken"));
    }
    @Test
    void createGamePositive() throws Exception {
        ChessGame game=new ChessGame();
        int gameID=dao.createGame(new GameData(0,null,null,"game1",game));
        GameData stored=dao.getGame(gameID);
        assertTrue(gameID>0);
        assertNotNull(stored);
        assertEquals(gameID,stored.gameID());
        assertEquals("game1",stored.gameName());
        assertEquals(game,stored.game());
    }
    @Test
    void createGameNegativeNull() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }
    @Test
    void getGamePositive() throws Exception {
        int gameID=dao.createGame(makeGame("game1"));
        GameData stored=dao.getGame(gameID);
        assertNotNull(stored);
        assertEquals(gameID, stored.gameID());
        assertEquals("game1",stored.gameName());
        assertNull(stored.whiteUsername());
        assertNull(stored.blackUsername());
    }
    @Test
    void getGameNegativeMissing() throws DataAccessException {
        GameData stored=dao.getGame(9999);
        assertNull(stored);
    }
    @Test
    void listGamesPositive() throws DataAccessException {
        dao.createGame(makeGame("game1"));
        dao.createGame(makeGame("game2"));
        Collection<GameData> games = dao.listGames();
        assertEquals(2,games.size());
    }
    @Test
    void listGamesNegativeEmptyDatabase() throws DataAccessException {
        Collection<GameData> games=dao.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }
    @Test
    void updateGamePositive() throws Exception {
        ChessGame game=new ChessGame();
        int gameID=dao.createGame(new GameData(0,null,null,"game1",game));
        ChessGame updatedChessGame=new ChessGame();
        updatedChessGame.makeMove(new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        ));
        GameData updated = new GameData(
                gameID,
                "whitePlayer",
                "blackPlayer",
                "updatedGame",
                updatedChessGame
        );
        dao.updateGame(updated);
        GameData stored=dao.getGame(gameID);
        assertNotNull(stored);
        assertEquals("whitePlayer", stored.whiteUsername());
        assertEquals("blackPlayer", stored.blackUsername());
        assertEquals("updatedGame", stored.gameName());
        assertEquals(ChessGame.TeamColor.BLACK, stored.game().getTeamTurn());
        assertNull(stored.game().getBoard().getPiece(new ChessPosition(2,5)));
        ChessPiece movedPiece = stored.game().getBoard().getPiece(new ChessPosition(4, 5));
        assertNotNull(movedPiece);
        assertEquals(ChessGame.TeamColor.WHITE, movedPiece.getTeamColor());
        assertEquals(ChessPiece.PieceType.PAWN, movedPiece.getPieceType());
    }
    @Test
    void updateGameNegativeMissingGame() {
        GameData missing = new GameData(9999,null,null,"missing",new ChessGame());
        assertThrows(DataAccessException.class,()->dao.updateGame(missing));
    }
}
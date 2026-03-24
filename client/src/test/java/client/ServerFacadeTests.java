package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade=new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }
    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }
    @AfterAll
    static void stopServer() {
        server.stop();
    }
    @Test
    public void registerPositive() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }
    @Test
    public void registerNegativeDuplicateUser() throws Exception {
        facade.register("player1","password","p1@email.com");
        assertThrows(Exception.class,()->
                facade.register("player1","password","p1@email.com"));
    }
    @Test
    public void loginPositive() throws Exception {
        facade.register("player1","password","p1@email.com");
        AuthData auth=facade.login("player1","password");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
    }
    @Test
    public void loginNegativeWrongPassword() throws Exception {
        facade.register("player1","password","p1@email.com");
        assertThrows(Exception.class,()->facade.login("player1","wrongPassword"));
    }
    @Test
    public void logoutPositive() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        assertDoesNotThrow(()->facade.logout(auth.authToken()));
    }
    @Test
    public void logoutNegativeBadToken() {
        assertThrows(Exception.class,()->facade.logout("bad-token"));
    }
    @Test
    public void createGamePositive() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        Integer gameID=facade.createGame(auth.authToken(),"My Game");
        assertNotNull(gameID);
        assertTrue(gameID>0);
    }
    @Test
    public void createGameNegativeUnauthorized() {
        assertThrows(Exception.class,()->facade.createGame("bad-token","My Game"));
    }
    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        facade.createGame(auth.authToken(),"Game One");
        List<GameData> games=facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(1,games.size());
        assertEquals("Game One",games.get(0).gameName());
    }
    @Test
    public void listGamesNegativeUnauthorized() {
        assertThrows(Exception.class,()->facade.listGames("bad-token"));
    }
    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        Integer gameID=facade.createGame(auth.authToken(),"Game One");
        assertDoesNotThrow(()->facade.joinGame(auth.authToken(),"WHITE",gameID));
    }
    @Test
    public void joinGameNegativeBadGame() throws Exception {
        AuthData auth=facade.register("player1","password","p1@email.com");
        assertThrows(Exception.class,()->facade.joinGame(auth.authToken(),"WHITE",999999));
    }
}
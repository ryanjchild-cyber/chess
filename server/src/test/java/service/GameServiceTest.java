package service;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private DataAccess dao;
    private UserService userService;
    private String auth1;
    private String auth2;
    @BeforeEach
    void setup() throws Exception {
        dao=new MemoryDataAccess();
        userService=new UserService(dao);
        gameService=new GameService(dao);
        auth1=userService.register(new RegisterRequest("u1","p1","u1@mail.com")).authToken();
        auth2=userService.register(new RegisterRequest("u2","p2","u2@mail.com")).authToken();
    }
    @Test
    void listGamesSuccess() throws Exception {
        gameService.createGame(auth1,new CreateGameRequest("g1"));
        gameService.createGame(auth2,new CreateGameRequest("g2"));
        ListGamesResult result=gameService.listGames(auth1);
        assertNotNull(result.games());
        assertEquals(2, result.games().size());
    }
    @Test
    void listGamesUnauthorizedFails() {
        assertThrows(UnauthorizedException.class,()-> gameService.listGames("not-a-real-token"));
    }
    @Test
    void createGameSuccess() throws Exception {
        CreateGameResult result=gameService.createGame(auth1,new CreateGameRequest("myGame"));
        assertTrue(result.gameID()>0);
    }
    @Test
    void joinGameSuccess() throws Exception {
        int id = gameService.createGame(auth1, new CreateGameRequest("g1")).gameID();
        gameService.joinGame(auth1, new JoinGameRequest("WHITE", id));
        ListGamesResult listed = gameService.listGames(auth1);
        GameSummary game = listed.games().get(0);
        assertEquals(id, game.gameID());
        assertEquals("u1", game.whiteUsername());
        assertNull(game.blackUsername());
    }
    @Test
    void joinGameAlreadyTakenFails() throws Exception {
        int id=gameService.createGame(auth1,new CreateGameRequest("g1")).gameID();
        gameService.joinGame(auth1,new JoinGameRequest("BLACK",id));
        assertThrows(ForbiddenException.class,()->gameService.joinGame(auth2,new JoinGameRequest("BLACK",id)));
    }
}

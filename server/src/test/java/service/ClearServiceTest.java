package service;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import service.services.ClearService;
import service.services.GameService;
import service.services.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private DataAccess dao;
    private ClearService clearService;
    private UserService userService;
    private GameService gameService;
    @BeforeEach
    void setup() {
        dao = new MemoryDataAccess();
        clearService = new ClearService(dao);
        userService = new UserService(dao);
        gameService = new GameService(dao);
    }
    @Test
    void clearSuccess() throws Exception {
        RegisterResult register=userService.register(new RegisterRequest("u","p","e@mail.com"));
        gameService.createGame(register.authToken(),new CreateGameRequest("g1"));
        clearService.clear();
        assertThrows(UnauthorizedException.class,()->gameService.listGames(register.authToken()));
    }
}

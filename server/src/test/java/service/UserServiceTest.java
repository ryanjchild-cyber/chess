package service;
import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import service.exceptions.ForbiddenException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;
import service.services.UserService;

public class UserServiceTest {
    private UserService userService;
    @BeforeEach
    void setup() {
        DataAccess dao = new MemoryDataAccess();
        userService=new UserService(dao);
    }
    @Test
    void registerSuccess() throws Exception {
        RegisterResult result=userService.register(new RegisterRequest("u","p","e@mail.com"));
        assertEquals("u",result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }
    @Test
    void registerDuplicateFails() throws Exception {
        userService.register(new RegisterRequest("u","p","e@mail.com"));
        assertThrows(ForbiddenException.class,
                ()->userService.register(new RegisterRequest("u","p2","e2@mail.com")));
    }
    @Test
    void loginSuccess() throws Exception {
        userService.register(new RegisterRequest("u","p","e@mail.com"));
        LoginResult result=userService.login(new LoginRequest("u","p"));
        assertEquals("u",result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }
    @Test
    void loginWrongPasswordFails() throws Exception {
        userService.register(new RegisterRequest("u","p","e@mail.com"));
        assertThrows(UnauthorizedException.class,
                ()->userService.login(new LoginRequest("u","wrong")));
    }
    @Test
    void logoutSuccess() throws Exception {
        String token=userService.register(new RegisterRequest("u","p","e@mail.com")).authToken();
        userService.logout(token);
        assertThrows(UnauthorizedException.class,
                ()->userService.logout(token));
    }
    @Test
    void logoutMissingTokenFails() {
        assertThrows(UnauthorizedException.class,
                ()->userService.logout(null));
    }
}

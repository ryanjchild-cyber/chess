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

}

package server;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import service.*;

public class Server {

    private final Javalin javalin;
    private final Gson gson=new Gson();
    private final DataAccess dao=new MemoryDataAccess();
    private final ClearService clearService=new ClearService(dao);
    private final UserService userService=new UserService(dao);
    private final GameService gameService=new GameService(dao);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

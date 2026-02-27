package server;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
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
        javalin.delete("/db",this::clear);

    }
    private void clear(Context context) throws DataAccessException {
        clearService.clear();
        okEmpty(context);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
    private void okEmpty(Context context) {
        context.status(200);
        context.contentType("application/json");
        context.result("{}");
    }
}

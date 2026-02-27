package server;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson=new Gson();
    private final DataAccess dao=new MemoryDataAccess();
    private final ClearService clearService=new ClearService(dao);
    private final UserService userService=new UserService(dao);
    private final GameService gameService=new GameService(dao);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        //endpoints
        javalin.delete("/db",this::clear);
        javalin.post("/user",this::register);
        javalin.post("/session",this::login);
        javalin.delete("/session",this::logout);
        javalin.get("/game",this::listGames);
        javalin.post("/game",this::createGame);
        javalin.put("/game",this::joinGame);
        //exceptions

    }
    private void clear(Context context) throws DataAccessException {
        clearService.clear();
        okEmpty(context);
    }
    private void register(Context context) throws DataAccessException {
        RegisterRequest request=gson.fromJson(context.body(),RegisterRequest.class);
        RegisterResult result=userService.register(request);
        okJson(context,result);
    }
    private void login(Context context) throws DataAccessException {
        LoginRequest request=gson.fromJson(context.body(), LoginRequest.class);
        LoginResult result=userService.login(request);
        okJson(context,result);
    }
    private void logout(Context context) throws DataAccessException {
        String authToken=context.header("authorization");
        userService.logout(authToken);
        okEmpty(context);
    }
    private void listGames(Context context) throws DataAccessException {
        String authToken=context.header("authorization");
        ListGamesResult result=gameService.listGames(authToken);
        okJson(context,result);
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
    private void okJson(Context context, Object object) {
        context.status(200);
        context.contentType("applicatoin/json");
        context.result(gson.toJson(object));
    }
}

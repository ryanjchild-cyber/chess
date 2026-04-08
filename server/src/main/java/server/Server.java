package server;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.exceptions.BadRequestException;
import service.exceptions.ForbiddenException;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import service.results.LoginResult;
import service.results.RegisterResult;
import service.services.ClearService;
import service.services.GameService;
import service.services.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson=new Gson();
    private final DataAccess dao;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    public Server() {
        try {
            dao=new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize database",e);
        }
        clearService=new ClearService(dao);
        userService=new UserService(dao);
        gameService=new GameService(dao);
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
        javalin.exception(BadRequestException.class, (e, context) -> {
            context.status(400);
            context.contentType("application/json");
            context.result(gson.toJson(Map.of("message","Error: bad request")));
        });
        javalin.exception(UnauthorizedException.class, (e, context) -> {
            context.status(401);
            context.contentType("application/json");
            context.result(gson.toJson(Map.of("message","Error: unauthorized")));
        });
        javalin.exception(ForbiddenException.class, (e, context) -> {
            context.status(403);
            context.contentType("application/json");
            context.result(gson.toJson(Map.of("message","Error: already taken")));
        });
        javalin.exception(DataAccessException.class, (e,context) -> {
            context.status(500);
            context.contentType("application/json");
            context.result(gson.toJson(Map.of("message","Error: "+safeMessage(e))));
        });
        javalin.exception(Exception.class, (e, context) -> {
            context.status(500);
            context.contentType("application/json");
            context.result(gson.toJson(Map.of("message", "Error: "+safeMessage(e))));
        });
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
    private void createGame(Context context) throws DataAccessException {
        String authToken=context.header("authorization");
        CreateGameRequest request=gson.fromJson(context.body(),CreateGameRequest.class);
        CreateGameResult result=gameService.createGame(authToken,request);
        okJson(context,result);
    }
    private void joinGame(Context context) throws DataAccessException {
        String authToken=context.header("authorization");
        JoinGameRequest request=gson.fromJson(context.body(),JoinGameRequest.class);
        gameService.joinGame(authToken,request);
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
    private void okJson(Context context, Object object) {
        context.status(200);
        context.contentType("application/json");
        context.result(gson.toJson(object));
    }
    private static String safeMessage(Exception e) {
        return (e.getMessage()==null||e.getMessage().isBlank())?"server error":e.getMessage();
    }
}
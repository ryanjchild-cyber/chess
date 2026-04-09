package ui;
import chess.ChessGame;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import client.WebSocketFacade;
import chess.ChessMove;
import chess.ChessPosition;
import websocket.messages.*;
import com.google.gson.Gson;
public class Repl {
    private final ServerFacade server;
    private final Scanner scanner=new Scanner(System.in);
    private AuthData auth;
    private final List<GameData> lastListedGames=new ArrayList<>();
    private WebSocketFacade ws;
    private final Gson gson=new Gson();
    private GameData currentGame;
    private int currentGameID;
    private ChessGame.TeamColor perspective;
    private boolean inGame=false;
    public Repl(ServerFacade server) {
        this.server=server;
    }
    public void run() {
        System.out.println("Welcome to 240 Chess");
        boolean running=true;
        while(running) {
            try {
                if (auth==null) {
                    running=runPrelogin();
                } else {
                    running=runPostLogin();
                }
            } catch(Exception ex) {
                System.out.println("Error: "+cleanMessage(ex.getMessage()));
            }
        }
        System.out.println("Goodbye.");
    }
    private boolean runPrelogin() {
        System.out.print("[LOGGED_OUT] >>> ");
        String input=scanner.nextLine().trim();
        if (input.isEmpty()) {
            return true;
        }
        String command=input.toLowerCase(Locale.ROOT);
        switch (command) {
            case "help"->printPreloginHelp();
            case "quit"->{return false;}
            case "login"->login();
            case "register"->register();
            default->System.out.println("Unknown command. Type help.");
        }
        return true;
    }
    private boolean runPostLogin() {
        System.out.print("[LOGGED_IN] >>> ");
        String input=scanner.nextLine().trim();
        if (input.isEmpty()) {
            return true;
        }
        String command=input.toLowerCase(Locale.ROOT);
        switch (command) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create game" -> createGame();
            case "list games" -> listGames();
            case "play game" -> playGame();
            case "observe game" -> observeGame();
            case "quit" -> { return false; }
            default -> System.out.println("Unknown command. Type help.");
        }
        return true;
    }
    private void register() {
        System.out.print("username: ");
        String username=scanner.nextLine().trim();
        System.out.print("password: ");
        String password=scanner.nextLine().trim();
        System.out.print("email: ");
        String email=scanner.nextLine().trim();
        if (username.isBlank()||password.isBlank()||email.isBlank()) {
            System.out.println("All fields are required.");
            return;
        }
        try {
            auth=server.register(username,password,email);
            System.out.println("Registered and logged in as "+username);
        } catch (Exception ex) {
            System.out.println("Unable to register: "+cleanMessage(ex.getMessage()));
        }
    }
    private void login() {
        System.out.print("username: ");
        String username=scanner.nextLine().trim();
        System.out.print("password: ");
        String password=scanner.nextLine().trim();
        if (username.isBlank()||password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }
        try {
            auth=server.login(username,password);
            System.out.println("Logged in as "+username);
        } catch (Exception ex) {
            System.out.println("Unable to login: "+cleanMessage(ex.getMessage()));
        }
    }
    private void logout() {
        try {
            server.logout(auth.authToken());
            auth = null;
            lastListedGames.clear();
            System.out.println("Logged out.");
        } catch (Exception ex) {
            System.out.println("Unable to logout: " + cleanMessage(ex.getMessage()));
        }
    }
    private void createGame() {
        System.out.print("game name: ");
        String gameName=scanner.nextLine().trim();
        if (gameName.isBlank()) {
            System.out.println("Game name is required.");
            return;
        }
        try {
            server.createGame(auth.authToken(),gameName);
            System.out.println("Game created.");
        } catch (Exception ex) {
            System.out.println("Unable to create game: "+cleanMessage(ex.getMessage()));
        }
    }
    private void listGames() {
        try {
            lastListedGames.clear();
            lastListedGames.addAll(server.listGames(auth.authToken()));
            if (lastListedGames.isEmpty()) {
                System.out.println("No games found.");
                return;
            }
            for (int i=0;i<lastListedGames.size();i++) {
                GameData game=lastListedGames.get(i);
                String white=blankAsOpen(game.whiteUsername());
                String black=blankAsOpen(game.blackUsername());
                System.out.printf("%d. %s | White: %s | Black: %s%n",
                        i+1,game.gameName(),white,black);
            }
        } catch (Exception ex) {
            System.out.println("Unable to list games: "+cleanMessage(ex.getMessage()));
        }
    }
    private void playGame() {
        if (lastListedGames.isEmpty()) {
            System.out.println("List games first.");
            return;
        }
        System.out.print("game number: ");
        Integer number = readInt();
        if (number == null || number < 1 || number > lastListedGames.size()) {
            System.out.println("Invalid game number.");
            return;
        }
        System.out.print("color (WHITE or BLACK): ");
        String color = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        GameData selected = lastListedGames.get(number - 1);
        try {
            server.joinGame(auth.authToken(), color, selected.gameID());
            perspective = color.equals("WHITE") ?
                    ChessGame.TeamColor.WHITE :
                    ChessGame.TeamColor.BLACK;
            currentGameID = selected.gameID();
            connectWebSocket();
            inGame = true;
            gameplayLoop();
        } catch (Exception ex) {
            System.out.println("Unable to join game: " + cleanMessage(ex.getMessage()));
        }
    }
    private void observeGame() {
        if (lastListedGames.isEmpty()) {
            System.out.println("List games first.");
            return;
        }
        System.out.print("game number: ");
        Integer number = readInt();
        if (number == null || number < 1 || number > lastListedGames.size()) {
            System.out.println("Invalid game number.");
            return;
        }
        GameData selected = lastListedGames.get(number - 1);
        try {
            perspective = ChessGame.TeamColor.WHITE;
            currentGameID = selected.gameID();
            connectWebSocket();
            inGame = true;
            gameplayLoop();
        } catch (Exception ex) {
            System.out.println("Unable to observe: " + cleanMessage(ex.getMessage()));
        }
    }
    private Integer readInt() {
        String text=scanner.nextLine().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    private void printPreloginHelp() {
        System.out.println("""
                help         - show available commands
                login        - log into an existing account
                register     - create a new account
                quit         - exit the program
                """);
    }
    private void printPostloginHelp() {
        System.out.println("""
                help         - show available commands
                create game  - create a new game
                list games   - list existing games
                play game    - join a game as white or black
                observe game - observe a game
                logout       - log out
                quit         - exit the program
                """);
    }
    private String blankAsOpen(String name) {
        return (name==null||name.isBlank())?"<open>":name;
    }
    private String cleanMessage(String message) {
        if (message==null||message.isBlank()) {
            return "Something went wrong.";
        }
        return message.replace("Error: ","");
    }
    private void connectWebSocket() throws Exception {
        ws=new WebSocketFacade(server.getServerUrl(),this::handleMessage);
        ws.connect(auth.authToken(),currentGameID);
    }
    private void gameplayLoop() {
        printGameplayHelp();
        while (inGame) {
            System.out.print("[GAME] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "help" -> printGameplayHelp();
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "resign" -> resign();
                case "move" -> makeMove();
                case "highlight" -> highlight();
                default -> System.out.println("Unknown command.");
            }
        }
    }
    private void handleMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(message, LoadGameMessage.class);
                currentGame = msg.getGame();
                redraw();
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(message, NotificationMessage.class);
                System.out.println(msg.getMessage());
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(message, ErrorMessage.class);
                System.out.println(msg.getErrorMessage());
            }
        }
    }
    private void redraw() {
        if (currentGame!=null) {
            ChessBoardUI.draw(currentGame.game(),perspective);
        }
    }
    private void leave() {
        try {
            ws.leave(auth.authToken(), currentGameID);
            ws.close();
            inGame = false;
        } catch (Exception e) {
            System.out.println("Error leaving game.");
        }
    }
    private void resign() {
        System.out.print("Are you sure? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            try {
                ws.resign(auth.authToken(), currentGameID);
            } catch (Exception e) {
                System.out.println("Error resigning.");
            }
        }
    }
    private void makeMove() {
        try {
            System.out.print("from (row col): ");
            int r1 = scanner.nextInt();
            int c1 = scanner.nextInt();
            System.out.print("to (row col): ");
            int r2 = scanner.nextInt();
            int c2 = scanner.nextInt();
            scanner.nextLine();
            ChessMove move = new ChessMove(
                    new ChessPosition(r1, c1),
                    new ChessPosition(r2, c2),
                    null
            );
            ws.makeMove(auth.authToken(), currentGameID, move);
        } catch (Exception e) {
            System.out.println("Invalid move input.");
            scanner.nextLine();
        }
    }
    private void highlight() {
        try {
            System.out.print("piece (row col): ");
            int r = scanner.nextInt();
            int c = scanner.nextInt();
            scanner.nextLine();
            var pos = new ChessPosition(r, c);
            var moves = currentGame.game().validMoves(pos);
            ChessBoardUI.draw(currentGame.game(), perspective, pos, moves);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }
    private void printGameplayHelp() {
        System.out.println("""
            help       - show commands
            redraw     - redraw board
            leave      - leave game
            move       - make a move
            resign     - resign game
            highlight  - highlight moves
            """);
    }
}

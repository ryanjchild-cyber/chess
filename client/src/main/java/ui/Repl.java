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
    private final Object consoleLock = new Object();
    private volatile boolean promptShowing = false;
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
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("Invalid color.");
            return;
        }
        GameData selected = lastListedGames.get(number - 1);
        boolean alreadyThisPlayer =
                (color.equals("WHITE") && auth.username().equals(selected.whiteUsername())) ||
                        (color.equals("BLACK") && auth.username().equals(selected.blackUsername()));
        boolean seatTakenBySomeoneElse =
                (color.equals("WHITE") && selected.whiteUsername() != null &&
                        !auth.username().equals(selected.whiteUsername())) ||
                        (color.equals("BLACK") && selected.blackUsername() != null &&
                                !auth.username().equals(selected.blackUsername()));
        if (seatTakenBySomeoneElse) {
            System.out.println("That color is already taken.");
            return;
        }
        try {
            if (!alreadyThisPlayer) {
                server.joinGame(auth.authToken(), color, selected.gameID());
            }
            perspective = color.equals("WHITE")
                    ? ChessGame.TeamColor.WHITE
                    : ChessGame.TeamColor.BLACK;
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
        boolean alreadyPlaying =
                auth.username().equals(selected.whiteUsername()) ||
                        auth.username().equals(selected.blackUsername());
        if (alreadyPlaying) {
            System.out.println("You are already a player in that game. Use play game instead.");
            return;
        }
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
            synchronized (consoleLock) {
                promptShowing = true;
                System.out.print("[GAME] >>> ");
                System.out.flush();
            }
            String input = scanner.nextLine().trim().toLowerCase();
            synchronized (consoleLock) {
                promptShowing = false;
            }
            switch (input) {
                case "help" -> printGameplayHelp();
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "resign" -> resign();
                case "move" -> makeMove();
                case "highlight" -> highlight();
                default -> {
                    synchronized (consoleLock) {
                        System.out.println("Unknown command.");
                    }
                }
            }
        }
    }
    private void handleMessage(String message) {
        synchronized (consoleLock) {
            ServerMessage base = gson.fromJson(message, ServerMessage.class);
            switch (base.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage msg = gson.fromJson(message, LoadGameMessage.class);
                    currentGame = msg.getGame();
                    ChessBoardUI.draw(currentGame.game(), perspective);
                    reprintPrompt();
                }
                case NOTIFICATION -> {
                    NotificationMessage msg = gson.fromJson(message, NotificationMessage.class);
                    System.out.println();
                    System.out.println(msg.getMessage());
                    reprintPrompt();
                }
                case ERROR -> {
                    ErrorMessage msg = gson.fromJson(message, ErrorMessage.class);
                    System.out.println();
                    System.out.println(msg.getErrorMessage());
                    reprintPrompt();
                }
            }
        }
    }
    private void redraw() {
        synchronized (consoleLock) {
            if (currentGame != null) {
                ChessBoardUI.draw(currentGame.game(), perspective);
                reprintPrompt();
            }
        }
    }
    private void leave() {
        try {
            ensureGameplayConnection();
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
                ensureGameplayConnection();
                ws.resign(auth.authToken(), currentGameID);
            } catch (Exception e) {
                System.out.println("Error resigning.");
            }
        }
    }
    private void makeMove() {
        try {
            synchronized (consoleLock) {
                System.out.print("from: ");
            }
            String fromInput = scanner.nextLine();
            synchronized (consoleLock) {
                System.out.print("to: ");
            }
            String toInput = scanner.nextLine();
            ChessPosition from = parsePosition(fromInput);
            ChessPosition to = parsePosition(toInput);
            ChessMove move = new ChessMove(from, to, null);
            ws.makeMove(auth.authToken(), currentGameID, move);
        } catch (Exception e) {
            synchronized (consoleLock) {
                System.out.println("Invalid move input.");
            }
        }
    }
    private void highlight() {
        try {
            synchronized (consoleLock) {
                System.out.print("piece: ");
            }
            String input = scanner.nextLine();

            ChessPosition pos = parsePosition(input);
            var moves = currentGame.game().validMoves(pos);
            synchronized (consoleLock) {
                ChessBoardUI.draw(currentGame.game(), perspective, pos, moves);
                reprintPrompt();
            }
        } catch (Exception e) {
            synchronized (consoleLock) {
                System.out.println("Invalid input.");
            }
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
    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid format");
        }
        char file = pos.charAt(0); // a-h
        char rank = pos.charAt(1); // 1-8
        int col = file - 'a' + 1;
        int row = Character.getNumericValue(rank);
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Out of bounds");
        }
        return new ChessPosition(row, col);
    }
    private void ensureGameplayConnection() throws Exception {
        if (ws == null || !ws.isOpen()) {
            ws = new WebSocketFacade(server.getServerUrl(), this::handleMessage);
            ws.connect(auth.authToken(), currentGameID);
        }
    }
    private void reprintPrompt() {
        if (inGame) {
            System.out.print("[GAME] >>> ");
            System.out.flush();
            promptShowing = true;
        }
    }
}
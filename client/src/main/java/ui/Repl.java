package ui;
import chess.ChessGame;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
public class Repl {
    private final ServerFacade server;
    private final Scanner scanner=new Scanner(System.in);
    private AuthData auth;
    private final List<GameData> lastListedGames=new ArrayList<>();
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
    private logout() {
        try {
            server.logout(auth.authToken());
            auth=null;
            lastListedGames.clear();
            System.out.println("Logged out.");
        } catch (Exception ex) {
            System.out.println("Unable to logout: "+cleanMessage(ex.getMessage()));
        }
    }
    private void printPreloginHelp() {
        System.out.println("""
                help         - show available commands
                login        -log into an existing account
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
}

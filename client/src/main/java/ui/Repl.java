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
}

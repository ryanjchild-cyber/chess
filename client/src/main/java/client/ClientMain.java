package client;
import ui.Repl;
import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);
        int port=8080;
        if (args.length>0) {
            try {
                port=Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Using default port 8080");
            }
        }
        var serverFacade=new ServerFacade(port);
        var repl=new Repl(serverFacade);
        repl.run();
    }
}

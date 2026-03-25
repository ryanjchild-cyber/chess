package ui;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import static ui.EscapeSequences.*;
public class ChessBoardUI {
    public static void draw(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board=game.getBoard();
        System.out.println();
        if (perspective==ChessGame.TeamColor.WHITE) {
            printLettersWhite();
            for (int row=8;row>=1;row--) {
                printRow(board,row,false);
            }
            printLettersWhite();
        } else {
            printLettersBlack();
            for (int row=1;row<=8;row++) {
                printRow(board,row,true);
            }
            printLettersBlack();
        }
        System.out.print(RESET_BG_COLOR);
        System.out.print(RESET_TEXT_COLOR);
        System.out.println();
    }
    private static void printLettersWhite() {}
}

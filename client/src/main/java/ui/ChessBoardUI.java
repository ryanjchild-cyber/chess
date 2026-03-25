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
    private static void printLettersWhite() {
        System.out.print(SET_BG_COLOR_BLACK+SET_TEXT_COLOR_WHITE+"    ");
        for (char c='a';c<='h';c++) {
            System.out.print(" "+c+" ");
        }
        System.out.println("    "+RESET_BG_COLOR+RESET_TEXT_COLOR);
    }
    private static void printLettersBlack() {
        System.out.print(SET_BG_COLOR_BLACK+SET_TEXT_COLOR_WHITE+"    ");
        for (char c='h';c>='a';c--) {
            System.out.print(" "+c+" ");
        }
        System.out.println("    "+RESET_BG_COLOR+RESET_TEXT_COLOR);
    }
    private static void printRow(ChessBoard board,int row,boolean blackPerspective) {
        System.out.print(SET_BG_COLOR_BLACK+SET_TEXT_COLOR_WHITE+" "+row+"  ");
        if (!blackPerspective) {
            for (int col=1;col<=8;col++) {
                printSquare(board,row,col);
            }
        } else {
            for (int col=8;col>=1;col--) {
                printSquare(board,row,col);
            }
        }
        System.out.println(SET_BG_COLOR_BLACK+SET_TEXT_COLOR_WHITE+"  "+row+" "+RESET_BG_COLOR+RESET_TEXT_COLOR);
    }
    private static void printSquare(ChessBoard board,int row,int col) {
        boolean lightSquare=(row+col)%2==1;
        if (lightSquare) {
            System.out.print(SET_BG_COLOR_WHITE);
        } else {
            System.out.print(SET_BG_COLOR_DARK_GREEN);
        }
        ChessPiece piece=board.getPiece(new ChessPosition(row,col));
        if (piece==null) {
            System.out.print(EMPTY);
        } else {
            System.out.print(pieceString(piece));
        }
        System.out.print(RESET_TEXT_COLOR);
    }
    private static String pieceString(ChessPiece piece) {
        boolean white=piece.getTeamColor()==ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING->white?SET_TEXT_COLOR_RED+WHITE_KING:SET_TEXT_COLOR_BLUE+BLACK_KING;
            case QUEEN->white?SET_TEXT_COLOR_RED+WHITE_QUEEN:SET_TEXT_COLOR_BLUE+BLACK_QUEEN;
            case BISHOP->white?SET_TEXT_COLOR_RED+WHITE_BISHOP:SET_TEXT_COLOR_BLUE+BLACK_BISHOP;
            case KNIGHT->white?SET_TEXT_COLOR_RED+WHITE_KNIGHT:SET_TEXT_COLOR_BLUE+BLACK_KNIGHT;
            case ROOK->white?SET_TEXT_COLOR_RED+WHITE_ROOK:SET_TEXT_COLOR_BLUE+BLACK_ROOK;
            case PAWN->white?SET_TEXT_COLOR_RED+WHITE_PAWN:SET_TEXT_COLOR_BLUE+BLACK_PAWN;
        };
    }
}

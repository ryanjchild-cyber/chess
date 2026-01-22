package chess;

import java.util.Arrays;
import java.util.Objects;

public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    public void resetBoard() {
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(2,col),
                    new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7,col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            ChessPiece.PieceType[] backRank = {
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.KNIGHT,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.KING,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT,
                    ChessPiece.PieceType.ROOK
            };
            for (int column = 1; column <= 8; column++) {
                addPiece(new ChessPosition(1, column),
                        new ChessPiece(ChessGame.TeamColor.WHITE, backRank[column - 1]));
                addPiece(new ChessPosition(8, column),
                        new ChessPiece(ChessGame.TeamColor.BLACK, backRank[column - 1]));
            }
        }
    }
}

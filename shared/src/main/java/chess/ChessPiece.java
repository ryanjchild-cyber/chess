package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor=pieceColor;
        this.type=type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves=new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType()==PieceType.BISHOP) {
            int[][] directions={
                    {1,1},
                    {1,-1},
                    {-1,1},
                    {-1,-1}
            };
            for (int[] movement: directions) {
                int row=myPosition.getRow()+movement[0];
                int col=myPosition.getColumn()+movement[1];
                while (row>=1 && row<=8 && col>=1 && col<=8) {
                    ChessPosition newPosition=new ChessPosition(row,col);
                    ChessPiece target=board.getPiece(newPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition,newPosition,null));
                    } else {
                        if (target.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition,newPosition, null));
                        }
                        break;
                    }
                    row+=movement[0];
                    col+=movement[1];
                }
            }
        }
        if (piece.getPieceType()==PieceType.KING) {
            int[][] directions={
                    {1,1},{0,1},
                    {1,-1},{1,0},
                    {-1,1},{0,-1},
                    {-1,-1},{-1,0}
            };
            for (int[] movement: directions) {
                int row=myPosition.getRow()+movement[0];
                int col=myPosition.getColumn()+movement[1];
                if (row<1 || row>8 || col<1 || col>8) {
                    break;
                }
                ChessPosition newPosition=new ChessPosition(row,col);
                ChessPiece target=board.getPiece(newPosition);
                if (target == null) {
                    moves.add(new ChessMove(myPosition,newPosition,null));
                } else if (target.getTeamColor() != piece.getTeamColor()){
                    moves.add(new ChessMove(myPosition,newPosition,null));
                }
            }
        }
        return moves;
    }
}

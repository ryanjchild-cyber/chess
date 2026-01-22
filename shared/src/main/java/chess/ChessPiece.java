package chess;

import java.util.ArrayList;
import java.util.Collection;
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
        this.pieceColor = pieceColor;
        this.type = type;
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

    private void pawnPromotion(Collection<ChessMove> moves, ChessPosition start,
                               ChessPosition end, int promotion) {
        if (end.getRow() == promotion) {
            moves.add(new ChessMove(start,end,PieceType.QUEEN));
            moves.add(new ChessMove(start,end,PieceType.ROOK));
            moves.add(new ChessMove(start,end,PieceType.BISHOP));
            moves.add(new ChessMove(start,end,PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start,end,null));
        }
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            int[][] directions = {
                    {1,1},
                    {1,-1},
                    {-1,1},
                    {-1,-1}
            };
            for (int[] movement:directions) {
                int row = myPosition.getRow() + movement[0];
                int col = myPosition.getColumn() + movement[1];
                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPosition = new ChessPosition(row,col);
                    ChessPiece target = board.getPiece(newPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition,newPosition,null));
                    } else {
                        if (target.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition,newPosition, null));
                        }
                        break;
                    }
                    row += movement[0];
                    col += movement[1];
                }
            }
        }
        if (piece.getPieceType() == PieceType.KING) {
            int[][] directions = {
                    {1,1},{0,1},
                    {1,-1},{1,0},
                    {-1,1},{0,-1},
                    {-1,-1},{-1,0}
            };
            for (int[] movement:directions) {
                int row = myPosition.getRow() + movement[0];
                int col = myPosition.getColumn() + movement[1];
                if (row<1 || row>8 || col<1 || col>8) {
                    break;
                }
                ChessPosition newPosition = new ChessPosition(row,col);
                ChessPiece target = board.getPiece(newPosition);
                if (target == null) {
                    moves.add(new ChessMove(myPosition,newPosition,null));
                } else if (target.getTeamColor() != piece.getTeamColor()){
                    moves.add(new ChessMove(myPosition,newPosition,null));
                }
            }
        }
        if (piece.getPieceType() == PieceType.KNIGHT) {
            int[][] directions = {
                    {2,1},{2,-1},
                    {-2,1},{-2,-1},
                    {1,2},{1,-2},
                    {-1,2},{-1,-2}
            };
            for (int[] movement:directions) {
                int row = myPosition.getRow() + movement[0];
                int col = myPosition.getColumn() + movement[1];
                if (row<1 || row>8 || col<1 || col>8) {
                    continue;    //apparently changing from break to continue made all the difference
                }
                ChessPosition newPosition = new ChessPosition(row,col);
                ChessPiece target = board.getPiece(newPosition);
                if (target == null || target.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition,newPosition,null));
                }
            }
        }
        if (piece.getPieceType() == PieceType.PAWN) {
            int directions = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)?1:-1;
            int start = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)?2:7;
            int promotion = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)?8:1;
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            int once = row+directions;
            if (once >= 1 && once <= 8) {
                ChessPosition singleStep = new ChessPosition(once, col);
                if (board.getPiece(singleStep) == null) {
                    pawnPromotion(moves, myPosition, singleStep, promotion);
                    if (row == start) {
                        int twice = row + 2 * directions;
                        ChessPosition doubleStep = new ChessPosition(twice, col);
                        if (board.getPiece(doubleStep) == null) {
                            moves.add(new ChessMove(myPosition, doubleStep, null));
                        }
                    }
                }
            }
            int[] diagonal = {-1,1};
            for (int diagonalColumn:diagonal) {
                int captureRow = row+directions;
                int captureCol = col+diagonalColumn;
                if(captureRow<1 || captureRow>8 || captureCol<1 || captureCol>8) {
                    break;
                }
                ChessPosition capturePosition = new ChessPosition(captureRow,captureCol);
                ChessPiece target = board.getPiece(capturePosition);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    pawnPromotion(moves,myPosition,capturePosition,promotion);
                }
            }
        }
        return moves;
    }
}

package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn=TeamColor.WHITE;
    private ChessBoard board= new ChessBoard();

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn=team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece=board.getPiece(startPosition);
        if (piece==null) {return null;}
        Collection<ChessMove> moveOptions=piece.pieceMoves(board,startPosition);
        Collection<ChessMove> moves=new ArrayList<>();
        for (ChessMove move:moveOptions) {
            ChessBoard copy=copyBoard(board);
            applyMove(copy,move);
            if (!isInCheckOnBoard(copy, piece.getTeamColor())) {
                moves.add(move);
            }
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board=board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy=new ChessBoard();
        for (int r=1; r<=8; r++) {
            for (int c=1; c<=8; c++) {
                ChessPosition position= new ChessPosition(r,c);
                ChessPiece piece=original.getPiece(position);
                if (piece==null) {
                    copy.addPiece(position,null);
                } else {
                    copy.addPiece(position,new ChessPiece(piece.getTeamColor(),piece.getPieceType()));
                }
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard board, ChessMove move) {
        ChessPosition start=move.getStartPosition();
        ChessPosition end=move.getEndPosition();
        ChessPiece moving=board.getPiece(start);
        board.addPiece(start,null);
        if (move.getPromotionPiece() != null) {
            moving=new ChessPiece(moving.getTeamColor(),move.getPromotionPiece());
        }
        board.addPiece(end,moving);
    }

    private boolean isInCheckOnBoard(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition=findKing(board,teamColor);
        if (kingPosition==null) {
            return false;
        }
        TeamColor opponent=(teamColor==TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition position = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != opponent) continue;
                for (ChessMove m : piece.pieceMoves(board, position)) {
                    if (kingPosition.equals(m.getEndPosition())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition position = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(position);
                //if (piece == null) continue;
                if (piece.getTeamColor()==teamColor&&piece.getPieceType()==ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }
}

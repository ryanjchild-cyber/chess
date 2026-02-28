package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {
    private TeamColor teamTurn=TeamColor.WHITE;
    private ChessBoard board= new ChessBoard();

    public ChessGame() {
        board.resetBoard();
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn=team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

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

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move==null) {throw new InvalidMoveException("Move is null");}
        ChessPosition start=move.getStartPosition();
        ChessPiece piece = board.getPiece(start);
        if (piece==null) {throw new InvalidMoveException("No piece at start position");}
        if (piece.getTeamColor()!=teamTurn) {throw new InvalidMoveException("Not your turn");}
        Collection<ChessMove> legalMoves=validMoves(start);
        if (legalMoves==null||!containsMove(legalMoves,move)) {
            throw new InvalidMoveException("Illegal move");
        }
        applyMove(board,move);
        teamTurn=(teamTurn==TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckOnBoard(board,teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {return false;}
        return !teamHasLegalMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {return false;}
        return !teamHasLegalMoves(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board=board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy=new ChessBoard();
        for(int r=1;r<=8;r++) {
            for (int c=1;c<=8;c++) {
                copy.addPiece(new ChessPosition(r,c),null);
            }
        }
        for (int r=1; r<=8; r++) {
            for (int c=1; c<=8; c++) {
                ChessPosition position= new ChessPosition(r,c);
                ChessPiece piece=original.getPiece(position);
                if (piece != null) {
                    copy.addPiece(position, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
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
            moving= new ChessPiece(moving.getTeamColor(),move.getPromotionPiece());
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
                if (piece == null || piece.getTeamColor() != opponent) {continue;}
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
                if (piece == null) {continue;}
                if (piece.getTeamColor()==teamColor&&piece.getPieceType()==ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean containsMove(Collection<ChessMove> moves,ChessMove target) {
        for (ChessMove move:moves) {
            if (move.equals(target)) {return true;}
        }
        return false;
    }

    private boolean teamHasLegalMoves(TeamColor teamColor) {
        for (int r=1;r<=8;r++) {
            for (int c=1;c<=8;c++) {
                ChessPosition position=new ChessPosition(r,c);
                ChessPiece piece=board.getPiece(position);
                if (piece==null||piece.getTeamColor()!=teamColor) {continue;}
                Collection<ChessMove> moves=validMoves(position);
                if (moves!=null&&!moves.isEmpty()) {return true;}
            }
        }
        return false;
    }
}

package org.example.chess.models;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] grid = new Piece[8][8];
    private Piece.Color turn = Piece.Color.WHITE;
    private int enPassantTargetX = -1;
    private int enPassantTargetY = -1;
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;

    public Board() { setupInitialBoard(); }

    private Board(boolean empty) {}

    private void setupInitialBoard() {
        for (int x = 0; x < 8; x++) {
            grid[x][1] = new Piece(Piece.Type.PAWN, Piece.Color.BLACK);
            grid[x][6] = new Piece(Piece.Type.PAWN, Piece.Color.WHITE);
        }
        placeBackRank(0, Piece.Color.BLACK);
        placeBackRank(7, Piece.Color.WHITE);
    }

    private void placeBackRank(int y, Piece.Color color) {
        grid[0][y] = new Piece(Piece.Type.ROOK, color);
        grid[1][y] = new Piece(Piece.Type.KNIGHT, color);
        grid[2][y] = new Piece(Piece.Type.BISHOP, color);
        grid[3][y] = new Piece(Piece.Type.QUEEN, color);
        grid[4][y] = new Piece(Piece.Type.KING, color);
        grid[5][y] = new Piece(Piece.Type.BISHOP, color);
        grid[6][y] = new Piece(Piece.Type.KNIGHT, color);
        grid[7][y] = new Piece(Piece.Type.ROOK, color);
    }

    public Piece getPiece(int x, int y) {
        if (!inside(x, y)) return null;
        return grid[x][y];
    }

    public void setPiece(int x, int y, Piece p) {
        if (inside(x, y)) grid[x][y] = p;
    }

    public Piece.Color getTurn() { return turn; }
    public void switchTurn() { turn = opposite(turn); }

    public boolean makeMove(Move move) {
        if (!isLegalMove(move.getFromX(), move.getFromY(), move.getToX(), move.getToY())) return false;
        executeMove(move);
        switchTurn();
        return true;
    }

    public List<Move> getValidMovesForPiece(int x, int y) {
        List<Move> moves = new ArrayList<>();
        Piece piece = getPiece(x, y);
        if (piece == null || piece.getColor() != turn) return moves;
        for (Move move : getPseudoMoves(x, y)) {
            Board copy = copy();
            copy.executeMove(move);
            if (!copy.isInCheck(piece.getColor())) moves.add(move);
        }
        return moves;
    }

    public List<Move> getAllLegalMoves(Piece.Color color) {
        Piece.Color oldTurn = turn;
        turn = color;
        List<Move> moves = new ArrayList<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece p = getPiece(x, y);
                if (p != null && p.getColor() == color) moves.addAll(getValidMovesForPiece(x, y));
            }
        }
        turn = oldTurn;
        return moves;
    }

    public boolean isValidRulesCheck(int fx, int fy, int tx, int ty) { return isLegalMove(fx, fy, tx, ty); }

    public boolean isLegalMove(int fx, int fy, int tx, int ty) {
        for (Move m : getValidMovesForPiece(fx, fy)) {
            if (m.getToX() == tx && m.getToY() == ty) return true;
        }
        return false;
    }

    private List<Move> getPseudoMoves(int x, int y) {
        List<Move> moves = new ArrayList<>();
        Piece p = getPiece(x, y);
        if (p == null) return moves;
        switch (p.getType()) {
            case PAWN -> pawnMoves(moves, x, y, p);
            case KNIGHT -> knightMoves(moves, x, y, p);
            case BISHOP -> slideMoves(moves, x, y, p, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
            case ROOK -> slideMoves(moves, x, y, p, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
            case QUEEN -> slideMoves(moves, x, y, p, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}});
            case KING -> kingMoves(moves, x, y, p);
        }
        return moves;
    }

    private void pawnMoves(List<Move> moves, int x, int y, Piece p) {
        int dir = p.getColor() == Piece.Color.WHITE ? -1 : 1;
        int start = p.getColor() == Piece.Color.WHITE ? 6 : 1;
        int promote = p.getColor() == Piece.Color.WHITE ? 0 : 7;
        if (inside(x, y + dir) && getPiece(x, y + dir) == null) {
            addPawnMove(moves, x, y, x, y + dir, promote);
            if (y == start && getPiece(x, y + dir * 2) == null) moves.add(new Move(x, y, x, y + dir * 2));
        }
        for (int dx : new int[]{-1, 1}) {
            int tx = x + dx;
            int ty = y + dir;
            if (!inside(tx, ty)) continue;
            Piece target = getPiece(tx, ty);
            if (target != null && target.getColor() != p.getColor() && target.getType() != Piece.Type.KING) addPawnMove(moves, x, y, tx, ty, promote);
            if (tx == enPassantTargetX && ty == enPassantTargetY) moves.add(new Move(x, y, tx, ty));
        }
    }

    private void addPawnMove(List<Move> moves, int fx, int fy, int tx, int ty, int promoteRank) {
        if (ty == promoteRank) {
            moves.add(new Move(fx, fy, tx, ty, Piece.Type.QUEEN));
            moves.add(new Move(fx, fy, tx, ty, Piece.Type.ROOK));
            moves.add(new Move(fx, fy, tx, ty, Piece.Type.BISHOP));
            moves.add(new Move(fx, fy, tx, ty, Piece.Type.KNIGHT));
        } else {
            moves.add(new Move(fx, fy, tx, ty));
        }
    }

    private void knightMoves(List<Move> moves, int x, int y, Piece p) {
        int[][] ds = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        for (int[] d : ds) addIfFreeOrEnemy(moves, x, y, x + d[0], y + d[1], p);
    }

    private void slideMoves(List<Move> moves, int x, int y, Piece p, int[][] dirs) {
        for (int[] d : dirs) {
            int tx = x + d[0];
            int ty = y + d[1];
            while (inside(tx, ty)) {
                Piece target = getPiece(tx, ty);
                if (target == null) {
                    moves.add(new Move(x, y, tx, ty));
                } else {
                    if (target.getColor() != p.getColor() && target.getType() != Piece.Type.KING) moves.add(new Move(x, y, tx, ty));
                    break;
                }
                tx += d[0];
                ty += d[1];
            }
        }
    }

    private void kingMoves(List<Move> moves, int x, int y, Piece p) {
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) if (dx != 0 || dy != 0) addIfFreeOrEnemy(moves, x, y, x + dx, y + dy, p);
        if (!p.hasMoved() && !isInCheck(p.getColor())) {
            int rank = p.getColor() == Piece.Color.WHITE ? 7 : 0;
            if (x == 4 && y == rank) {
                Piece rookK = getPiece(7, rank);
                if (rookK != null && rookK.getType() == Piece.Type.ROOK && rookK.getColor() == p.getColor() && !rookK.hasMoved() && getPiece(5, rank) == null && getPiece(6, rank) == null && !isSquareAttacked(5, rank, opposite(p.getColor())) && !isSquareAttacked(6, rank, opposite(p.getColor()))) moves.add(new Move(x, y, 6, rank));
                Piece rookQ = getPiece(0, rank);
                if (rookQ != null && rookQ.getType() == Piece.Type.ROOK && rookQ.getColor() == p.getColor() && !rookQ.hasMoved() && getPiece(1, rank) == null && getPiece(2, rank) == null && getPiece(3, rank) == null && !isSquareAttacked(3, rank, opposite(p.getColor())) && !isSquareAttacked(2, rank, opposite(p.getColor()))) moves.add(new Move(x, y, 2, rank));
            }
        }
    }

    private void addIfFreeOrEnemy(List<Move> moves, int fx, int fy, int tx, int ty, Piece p) {
        if (!inside(tx, ty)) return;
        Piece target = getPiece(tx, ty);
        if (target == null || (target.getColor() != p.getColor() && target.getType() != Piece.Type.KING)) moves.add(new Move(fx, fy, tx, ty));
    }

    public void executeMove(Move m) {
        Piece p = getPiece(m.getFromX(), m.getFromY());
        if (p == null) return;
        Piece captured = getPiece(m.getToX(), m.getToY());
        if (p.getType() == Piece.Type.PAWN && m.getToX() == enPassantTargetX && m.getToY() == enPassantTargetY && captured == null) setPiece(m.getToX(), m.getFromY(), null);
        if (p.getType() == Piece.Type.KING && Math.abs(m.getToX() - m.getFromX()) == 2) {
            if (m.getToX() == 6) {
                Piece rook = getPiece(7, m.getFromY());
                setPiece(5, m.getFromY(), rook);
                setPiece(7, m.getFromY(), null);
                if (rook != null) rook.setMoved(true);
            } else if (m.getToX() == 2) {
                Piece rook = getPiece(0, m.getFromY());
                setPiece(3, m.getFromY(), rook);
                setPiece(0, m.getFromY(), null);
                if (rook != null) rook.setMoved(true);
            }
        }
        setPiece(m.getToX(), m.getToY(), p);
        setPiece(m.getFromX(), m.getFromY(), null);
        p.setMoved(true);
        if (p.getType() == Piece.Type.PAWN && (m.getToY() == 0 || m.getToY() == 7)) p.setType(m.getPromotion() == null ? Piece.Type.QUEEN : m.getPromotion());
        if (p.getType() == Piece.Type.PAWN && Math.abs(m.getToY() - m.getFromY()) == 2) {
            enPassantTargetX = m.getFromX();
            enPassantTargetY = (m.getFromY() + m.getToY()) / 2;
        } else {
            enPassantTargetX = -1;
            enPassantTargetY = -1;
        }
        halfMoveClock = p.getType() == Piece.Type.PAWN || captured != null ? 0 : halfMoveClock + 1;
        if (turn == Piece.Color.BLACK) fullMoveNumber++;
    }

    public boolean isInCheck(Piece.Color color) {
        int[] king = findKing(color);
        return king == null || isSquareAttacked(king[0], king[1], opposite(color));
    }

    public boolean isCheckmate(Piece.Color color) { return isInCheck(color) && getAllLegalMoves(color).isEmpty(); }
    public boolean isStalemate(Piece.Color color) { return !isInCheck(color) && getAllLegalMoves(color).isEmpty(); }
    public boolean isDrawByFiftyMoveRule() { return halfMoveClock >= 100; }

    public boolean isKingDead(Piece.Color color) { return findKing(color) == null; }

    public int[] getKingPosition(Piece.Color color) {
        return findKing(color);
    }

    private boolean isSquareAttacked(int x, int y, Piece.Color byColor) {
        int pawnDir = byColor == Piece.Color.WHITE ? -1 : 1;
        for (int dx : new int[]{-1, 1}) {
            Piece pawn = getPiece(x - dx, y - pawnDir);
            if (pawn != null && pawn.getColor() == byColor && pawn.getType() == Piece.Type.PAWN) return true;
        }
        int[][] ns = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        for (int[] d : ns) {
            Piece n = getPiece(x + d[0], y + d[1]);
            if (n != null && n.getColor() == byColor && n.getType() == Piece.Type.KNIGHT) return true;
        }
        if (attackedBySlider(x, y, byColor, new int[][]{{1,0},{-1,0},{0,1},{0,-1}}, Piece.Type.ROOK, Piece.Type.QUEEN)) return true;
        if (attackedBySlider(x, y, byColor, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}, Piece.Type.BISHOP, Piece.Type.QUEEN)) return true;
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            Piece k = getPiece(x + dx, y + dy);
            if (k != null && k.getColor() == byColor && k.getType() == Piece.Type.KING) return true;
        }
        return false;
    }

    private boolean attackedBySlider(int x, int y, Piece.Color color, int[][] dirs, Piece.Type a, Piece.Type b) {
        for (int[] d : dirs) {
            int tx = x + d[0];
            int ty = y + d[1];
            while (inside(tx, ty)) {
                Piece p = getPiece(tx, ty);
                if (p != null) return p.getColor() == color && (p.getType() == a || p.getType() == b);
                tx += d[0];
                ty += d[1];
            }
        }
        return false;
    }

    private int[] findKing(Piece.Color color) {
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
            Piece p = getPiece(x, y);
            if (p != null && p.getColor() == color && p.getType() == Piece.Type.KING) return new int[]{x, y};
        }
        return null;
    }

    private Board copy() {
        Board b = new Board(true);
        b.grid = new Piece[8][8];
        for (int x = 0; x < 8; x++) for (int y = 0; y < 8; y++) if (grid[x][y] != null) b.grid[x][y] = grid[x][y].copy();
        b.turn = turn;
        b.enPassantTargetX = enPassantTargetX;
        b.enPassantTargetY = enPassantTargetY;
        b.halfMoveClock = halfMoveClock;
        b.fullMoveNumber = fullMoveNumber;
        return b;
    }

    public String generateFen() {
        StringBuilder fen = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            int empty = 0;
            for (int x = 0; x < 8; x++) {
                Piece p = grid[x][y];
                if (p == null) empty++; else {
                    if (empty > 0) { fen.append(empty); empty = 0; }
                    char c = switch (p.getType()) {
                        case PAWN -> 'p';
                        case ROOK -> 'r';
                        case KNIGHT -> 'n';
                        case BISHOP -> 'b';
                        case QUEEN -> 'q';
                        case KING -> 'k';
                    };
                    fen.append(p.getColor() == Piece.Color.WHITE ? Character.toUpperCase(c) : c);
                }
            }
            if (empty > 0) fen.append(empty);
            if (y < 7) fen.append('/');
        }
        fen.append(' ').append(turn == Piece.Color.WHITE ? 'w' : 'b').append(' ');
        String castling = castlingRights();
        fen.append(castling.isEmpty() ? "-" : castling).append(' ');
        fen.append(enPassantTargetX == -1 ? "-" : "" + (char)('a' + enPassantTargetX) + (8 - enPassantTargetY));
        fen.append(' ').append(halfMoveClock).append(' ').append(fullMoveNumber);
        return fen.toString();
    }

    private String castlingRights() {
        StringBuilder s = new StringBuilder();
        addCastling(s, Piece.Color.WHITE, 7, 'K', 'Q');
        addCastling(s, Piece.Color.BLACK, 0, 'k', 'q');
        return s.toString();
    }

    private void addCastling(StringBuilder s, Piece.Color color, int rank, char kingSide, char queenSide) {
        Piece king = getPiece(4, rank);
        if (king == null || king.getType() != Piece.Type.KING || king.getColor() != color || king.hasMoved()) return;
        Piece rookK = getPiece(7, rank);
        if (rookK != null && rookK.getType() == Piece.Type.ROOK && rookK.getColor() == color && !rookK.hasMoved()) s.append(kingSide);
        Piece rookQ = getPiece(0, rank);
        if (rookQ != null && rookQ.getType() == Piece.Type.ROOK && rookQ.getColor() == color && !rookQ.hasMoved()) s.append(queenSide);
    }

    private boolean inside(int x, int y) { return x >= 0 && x < 8 && y >= 0 && y < 8; }
    private Piece.Color opposite(Piece.Color color) { return color == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE; }
}

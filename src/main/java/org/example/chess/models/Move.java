package org.example.chess.models;

public class Move {
    private final int fromX, fromY, toX, toY;
    private final Piece.Type promotion;

    public Move(int fromX, int fromY, int toX, int toY) {
        this(fromX, fromY, toX, toY, null);
    }

    public Move(int fromX, int fromY, int toX, int toY, Piece.Type promotion) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.promotion = promotion;
    }

    public int getFromX() { return fromX; }
    public int getFromY() { return fromY; }
    public int getToX() { return toX; }
    public int getToY() { return toY; }
    public Piece.Type getPromotion() { return promotion; }

    public String toUCI() {
        String s = "" + (char)('a' + fromX) + (8 - fromY) + (char)('a' + toX) + (8 - toY);
        if (promotion != null) {
            s += switch (promotion) {
                case QUEEN -> "q";
                case ROOK -> "r";
                case BISHOP -> "b";
                case KNIGHT -> "n";
                default -> "q";
            };
        }
        return s;
    }

    public static Move fromUCI(String uci) {
        int fx = uci.charAt(0) - 'a';
        int fy = 8 - Character.getNumericValue(uci.charAt(1));
        int tx = uci.charAt(2) - 'a';
        int ty = 8 - Character.getNumericValue(uci.charAt(3));
        Piece.Type promo = null;
        if (uci.length() >= 5) {
            promo = switch (uci.charAt(4)) {
                case 'r' -> Piece.Type.ROOK;
                case 'b' -> Piece.Type.BISHOP;
                case 'n' -> Piece.Type.KNIGHT;
                default -> Piece.Type.QUEEN;
            };
        }
        return new Move(fx, fy, tx, ty, promo);
    }
}

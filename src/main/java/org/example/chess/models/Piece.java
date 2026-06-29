package org.example.chess.models;

public class Piece {
    public enum Type { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
    public enum Color { WHITE, BLACK }

    private Type type;
    private final Color color;
    private boolean hasMoved;

    public Piece(Type type, Color color) {
        this.type = type;
        this.color = color;
    }

    public Piece(Type type, Color color, boolean hasMoved) {
        this.type = type;
        this.color = color;
        this.hasMoved = hasMoved;
    }

    public Type getType() { return type; }
    public Color getColor() { return color; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved(boolean moved) { this.hasMoved = moved; }
    public void setType(Type type) { this.type = type; }
    public Piece copy() { return new Piece(type, color, hasMoved); }
}

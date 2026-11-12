package de.schachmod;

public record Piece(PieceType type, boolean white) {
    public String colorName() {
        return white ? "Weiß" : "Schwarz";
    }
}

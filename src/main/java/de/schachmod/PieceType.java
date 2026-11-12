package de.schachmod;

import java.util.Locale;

public enum PieceType {
    PAWN("Bauer", "pawn", "bauer", Shapes.PLUS, Shapes.DOT, Shapes.DOT),
    ROOK("Turm", "rook", "turm", Shapes.FULL, Shapes.PLUS, Shapes.FULL, Shapes.CORNERS),
    KNIGHT("Springer", "knight", "springer", Shapes.FULL, Shapes.LINE, Shapes.NECK, Shapes.HEAD),
    BISHOP("Läufer", "bishop", "laeufer", Shapes.PLUS, Shapes.DOT, Shapes.PLUS, Shapes.DOT, Shapes.DOT),
    QUEEN("Dame", "queen", "dame", Shapes.FULL, Shapes.PLUS, Shapes.DOT, Shapes.DOT, Shapes.PLUS, Shapes.RING),
    KING("König", "king", "koenig", Shapes.FULL, Shapes.PLUS, Shapes.DOT, Shapes.PLUS, Shapes.DOT, Shapes.PLUS, Shapes.DOT);

    private final String german;
    private final String english;
    private final String ascii;
    private final String[][] layers;

    PieceType(String german, String english, String ascii, String[]... layers) {
        this.german = german;
        this.english = english;
        this.ascii = ascii;
        this.layers = layers;
    }

    public String german() { return german; }
    public String[][] layers() { return layers; }

    public static PieceType fromName(String s) {
        String n = s.toLowerCase(Locale.ROOT);
        for (PieceType t : values()) {
            if (t.english.equals(n) || t.ascii.equals(n)) return t;
        }
        return null;
    }
}

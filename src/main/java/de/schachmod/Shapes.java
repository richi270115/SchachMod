package de.schachmod;

/** Bausteine fuer die Figuren: je 3x3 Bloecke pro Ebene ('#' = Block). Zeile 0 = Norden. */
final class Shapes {
    private Shapes() {}

    static final String[] FULL    = {"###", "###", "###"};
    static final String[] PLUS    = {".#.", "###", ".#."};
    static final String[] DOT     = {"...", ".#.", "..."};
    static final String[] RING    = {".#.", "#.#", ".#."};
    static final String[] CORNERS = {"#.#", "...", "#.#"};
    static final String[] LINE    = {".#.", ".#.", ".#."};
    static final String[] NECK    = {".#.", ".#.", "..."};
    static final String[] HEAD    = {".#.", "...", "..."};
}

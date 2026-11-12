package de.schachmod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class ChessBoard {
    public static final int CELL = 3;
    public static final int SIZE = 8 * CELL;
    public static final int CLEAR_HEIGHT = 8;

    private static final BlockState LIGHT = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState DARK = Blocks.GREEN_TERRACOTTA.defaultBlockState();
    private static final BlockState SELECT = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState FRAME = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState WHITE = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState BLACK = Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private record Snapshot(Piece[][] grid, boolean whiteToMove) {}

    private final ServerLevel level;
    private final BlockPos origin;
    private Piece[][] grid = new Piece[8][8];
    private boolean whiteToMove = true;
    private int selFile = -1, selRank = -1;
    private final Deque<Snapshot> history = new ArrayDeque<>();

    public ChessBoard(ServerLevel level, BlockPos origin) {
        this.level = level;
        this.origin = origin;
        reset();
    }

    public ServerLevel level() { return level; }
    public Piece get(int f, int r) { return grid[f][r]; }

    public void reset() {
        grid = new Piece[8][8];
        PieceType[] back = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int f = 0; f < 8; f++) {
            grid[f][0] = new Piece(back[f], true);
            grid[f][1] = new Piece(PieceType.PAWN, true);
            grid[f][6] = new Piece(PieceType.PAWN, false);
            grid[f][7] = new Piece(back[f], false);
        }
        whiteToMove = true;
        selFile = selRank = -1;
        history.clear();
        buildAll();
    }

    public boolean move(int f1, int r1, int f2, int r2) {
        Piece mover = grid[f1][r1];
        if (mover == null || (f1 == f2 && r1 == r2)) return false;
        Piece target = grid[f2][r2];
        if (target != null && target.white() == mover.white()) return false;
        deselect();
        pushHistory();
        grid[f2][r2] = mover;
        grid[f1][r1] = null;
        drawSquare(f1, r1);
        drawSquare(f2, r2);
        whiteToMove = !mover.white();
        String text = mover.colorName() + ": " + mover.type().german() + " "
                + name(f1, r1) + " → " + name(f2, r2);
        if (target != null) text += " (schlägt " + target.type().german() + ")";
        text += ". " + (whiteToMove ? "Weiß" : "Schwarz") + " ist am Zug.";
        chat(text);
        return true;
    }

    public void setPiece(int f, int r, Piece p) {
        deselect();
        pushHistory();
        grid[f][r] = p;
        drawSquare(f, r);
        chat(p == null ? "Feld " + name(f, r) + " geleert."
                : name(f, r) + ": " + p.colorName() + " " + p.type().german());
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        deselect();
        Snapshot s = history.pop();
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                if (!Objects.equals(grid[f][r], s.grid()[f][r])) {
                    grid[f][r] = s.grid()[f][r];
                    drawSquare(f, r);
                }
            }
        }
        whiteToMove = s.whiteToMove();
        chat("Zug zurückgenommen. " + (whiteToMove ? "Weiß" : "Schwarz") + " ist am Zug.");
        return true;
    }

    public void click(ServerPlayer player, int f, int r) {
        Piece p = grid[f][r];
        if (selFile < 0) {
            if (p == null) return;
            select(f, r);
            bar(player, "Ausgewählt: " + p.colorName() + " " + p.type().german() + " "
                    + name(f, r) + " – klicke auf das Zielfeld");
            return;
        }
        if (selFile == f && selRank == r) {
            deselect();
            bar(player, "Auswahl aufgehoben");
            return;
        }
        Piece sel = grid[selFile][selRank];
        if (p != null && sel != null && p.white() == sel.white()) {
            deselect();
            select(f, r);
            bar(player, "Ausgewählt: " + p.colorName() + " " + p.type().german() + " " + name(f, r));
            return;
        }
        move(selFile, selRank, f, r);
    }

    private void select(int f, int r) { selFile = f; selRank = r; drawSquare(f, r); }

    private void deselect() {
        if (selFile < 0) return;
        int f = selFile, r = selRank;
        selFile = selRank = -1;
        drawSquare(f, r);
    }

    public boolean contains(ServerLevel l, BlockPos p) {
        return l == level
                && p.getX() >= origin.getX() && p.getX() < origin.getX() + SIZE
                && p.getZ() >= origin.getZ() && p.getZ() < origin.getZ() + SIZE
                && p.getY() >= origin.getY() && p.getY() <= origin.getY() + CLEAR_HEIGHT;
    }

    public int fileOf(int x) { return (x - origin.getX()) / CELL; }
    public int rankOf(int z) { return 7 - (z - origin.getZ()) / CELL; }

    public double distanceSqr(double x, double y, double z) {
        double dx = x - (origin.getX() + SIZE / 2.0);
        double dy = y - origin.getY();
        double dz = z - (origin.getZ() + SIZE / 2.0);
        return dx * dx + dy * dy + dz * dz;
    }

    public static String name(int f, int r) { return "" + (char) ('a' + f) + (r + 1); }

    public static int[] parse(String s) {
        if (s == null || s.length() != 2) return null;
        int f = Character.toLowerCase(s.charAt(0)) - 'a';
        int r = s.charAt(1) - '1';
        if (f < 0 || f > 7 || r < 0 || r > 7) return null;
        return new int[]{f, r};
    }

    private void buildAll() {
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        for (int x = -1; x <= SIZE; x++) {
            for (int z = -1; z <= SIZE; z++) {
                boolean border = x < 0 || z < 0 || x >= SIZE || z >= SIZE;
                if (border) set(ox + x, oy, oz + z, FRAME);
                for (int dy = 1; dy <= CLEAR_HEIGHT; dy++) set(ox + x, oy + dy, oz + z, AIR);
            }
        }
        for (int f = 0; f < 8; f++) for (int r = 0; r < 8; r++) drawSquare(f, r);
    }

    public void demolish() {
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        for (int x = -1; x <= SIZE; x++) {
            for (int z = -1; z <= SIZE; z++) {
                set(ox + x, oy, oz + z, grass);
                for (int dy = 1; dy <= CLEAR_HEIGHT; dy++) set(ox + x, oy + dy, oz + z, AIR);
            }
        }
    }

    private void drawSquare(int f, int r) {
        int x0 = origin.getX() + f * CELL;
        int z0 = origin.getZ() + (7 - r) * CELL;
        int y = origin.getY();
        boolean dark = (f + r) % 2 == 0;
        BlockState floor = (f == selFile && r == selRank) ? SELECT : (dark ? DARK : LIGHT);
        for (int dx = 0; dx < CELL; dx++) {
            for (int dz = 0; dz < CELL; dz++) {
                set(x0 + dx, y, z0 + dz, floor);
                for (int dy = 1; dy <= CLEAR_HEIGHT; dy++) set(x0 + dx, y + dy, z0 + dz, AIR);
            }
        }
        Piece p = grid[f][r];
        if (p == null) return;
        BlockState material = p.white() ? WHITE : BLACK;
        boolean rotate = !p.white();
        String[][] layers = p.type().layers();
        for (int ly = 0; ly < layers.length; ly++) {
            for (int row = 0; row < CELL; row++) {
                for (int col = 0; col < CELL; col++) {
                    int rr = rotate ? 2 - row : row;
                    int cc = rotate ? 2 - col : col;
                    if (layers[ly][rr].charAt(cc) == '#') set(x0 + col, y + 1 + ly, z0 + row, material);
                }
            }
        }
    }

    private void set(int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    private void pushHistory() {
        Piece[][] copy = new Piece[8][];
        for (int i = 0; i < 8; i++) copy[i] = grid[i].clone();
        history.push(new Snapshot(copy, whiteToMove));
    }

    private void chat(String msg) {
        Component c = Component.literal(msg);
        for (ServerPlayer p : level.players()) if (distanceSqr(p.getX(), p.getY(), p.getZ()) < 100 * 100) p.sendSystemMessage(c);
    }

    private static void bar(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg), true);
    }
}

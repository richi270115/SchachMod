package de.schachmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class ChessManager {
    private static final List<ChessBoard> BOARDS = new ArrayList<>();
    private ChessManager() {}

    public static ChessBoard create(ServerLevel level, BlockPos origin) {
        ChessBoard b = new ChessBoard(level, origin);
        BOARDS.add(b);
        return b;
    }

    public static ChessBoard at(ServerLevel level, BlockPos pos) {
        for (ChessBoard b : BOARDS) if (b.contains(level, pos)) return b;
        return null;
    }

    public static ChessBoard nearest(ServerPlayer p) {
        ChessBoard best = null;
        double bestDist = Double.MAX_VALUE;
        for (ChessBoard b : BOARDS) {
            if (b.level() != p.level()) continue;
            double d = b.distanceSqr(p.getX(), p.getY(), p.getZ());
            if (d < bestDist) { bestDist = d; best = b; }
        }
        return best;
    }

    public static void delete(ChessBoard b) {
        b.demolish();
        BOARDS.remove(b);
    }

    public static void clear() { BOARDS.clear(); }
}

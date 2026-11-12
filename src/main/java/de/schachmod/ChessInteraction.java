package de.schachmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ChessInteraction {
    private ChessInteraction() {}

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        ChessBoard board = ChessManager.at((ServerLevel) world, hit.getBlockPos());
        if (board == null) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        int f = board.fileOf(hit.getBlockPos().getX());
        int r = board.rankOf(hit.getBlockPos().getZ());
        if (f < 0 || f > 7 || r < 0 || r > 7) return InteractionResult.PASS;

        board.click(sp, f, r);
        return InteractionResult.SUCCESS;
    }
}

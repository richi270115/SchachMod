package de.schachmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ChessCommands {
    private ChessCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("schach")
                .then(Commands.literal("neu").executes(ChessCommands::neu))
                .then(Commands.literal("zug")
                        .then(Commands.argument("von", StringArgumentType.word())
                                .then(Commands.argument("nach", StringArgumentType.word()).executes(ChessCommands::zug))))
                .then(Commands.literal("zurueck").executes(ChessCommands::zurueck))
                .then(Commands.literal("entfernen")
                        .then(Commands.argument("feld", StringArgumentType.word()).executes(ChessCommands::entfernen)))
                .then(Commands.literal("umwandeln")
                        .then(Commands.argument("feld", StringArgumentType.word())
                                .then(Commands.argument("figur", StringArgumentType.word()).executes(ChessCommands::umwandeln))))
                .then(Commands.literal("neustart").executes(ChessCommands::neustart))
                .then(Commands.literal("loeschen").executes(ChessCommands::loeschen)));
        dispatcher.register(Commands.literal("chess").redirect(node));
    }

    private static int neu(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = src.getPlayerOrException();
        ServerLevel level = src.getLevel();
        BlockPos pos = p.blockPosition();
        BlockPos origin = new BlockPos(pos.getX() - ChessBoard.SIZE / 2, pos.getY() - 1,
                pos.getZ() - ChessBoard.SIZE - 3);
        if (level.isOutsideBuildHeight(origin.above(ChessBoard.CLEAR_HEIGHT + 1)) || level.isOutsideBuildHeight(origin)) {
            src.sendFailure(Component.literal("Hier ist zu wenig Platz nach oben."));
            return 0;
        }
        ChessManager.create(level, origin);
        src.sendSuccess(() -> Component.literal(
                "Schachbrett erstellt! Rechtsklick auf eine Figur, dann auf das Zielfeld. Weiß beginnt."), true);
        return 1;
    }

    private static int zug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        int[] a = ChessBoard.parse(StringArgumentType.getString(ctx, "von"));
        int[] b = ChessBoard.parse(StringArgumentType.getString(ctx, "nach"));
        if (a == null || b == null) {
            ctx.getSource().sendFailure(Component.literal("Ungültiges Feld. Beispiel: /schach zug e2 e4"));
            return 0;
        }
        if (!board.move(a[0], a[1], b[0], b[1])) {
            ctx.getSource().sendFailure(Component.literal(
                    "Zug nicht möglich: Startfeld leer oder Zielfeld mit eigener Figur besetzt."));
            return 0;
        }
        return 1;
    }

    private static int zurueck(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        if (!board.undo()) {
            ctx.getSource().sendFailure(Component.literal("Es gibt nichts zurückzunehmen."));
            return 0;
        }
        return 1;
    }

    private static int entfernen(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        int[] sq = ChessBoard.parse(StringArgumentType.getString(ctx, "feld"));
        if (sq == null) {
            ctx.getSource().sendFailure(Component.literal("Ungültiges Feld. Beispiel: /schach entfernen e5"));
            return 0;
        }
        board.setPiece(sq[0], sq[1], null);
        return 1;
    }

    private static int umwandeln(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        int[] sq = ChessBoard.parse(StringArgumentType.getString(ctx, "feld"));
        PieceType type = PieceType.fromName(StringArgumentType.getString(ctx, "figur"));
        if (sq == null || type == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Beispiel: /schach umwandeln e8 dame (dame, turm, laeufer, springer, bauer, koenig)"));
            return 0;
        }
        Piece old = board.get(sq[0], sq[1]);
        if (old == null) {
            ctx.getSource().sendFailure(Component.literal("Auf diesem Feld steht keine Figur."));
            return 0;
        }
        board.setPiece(sq[0], sq[1], new Piece(type, old.white()));
        return 1;
    }

    private static int neustart(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        board.reset();
        ctx.getSource().sendSuccess(() -> Component.literal("Neue Partie gestartet."), true);
        return 1;
    }

    private static int loeschen(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChessBoard board = board(ctx);
        if (board == null) return 0;
        ChessManager.delete(board);
        ctx.getSource().sendSuccess(() -> Component.literal("Schachbrett entfernt."), true);
        return 1;
    }

    private static ChessBoard board(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ChessBoard b = ChessManager.nearest(p);
        if (b == null) ctx.getSource().sendFailure(Component.literal(
                "Kein Schachbrett gefunden. Erstelle eins mit /schach neu"));
        return b;
    }
}

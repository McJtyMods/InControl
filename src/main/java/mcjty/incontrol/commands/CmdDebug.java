package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.FabricEventHandlers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CmdDebug implements Command<CommandSourceStack> {

    private static final CmdDebug CMD = new CmdDebug();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("debug")
                .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player != null) {
            FabricEventHandlers.debug = !FabricEventHandlers.debug;
            if (FabricEventHandlers.debug) {
                player.sendSystemMessage(Component.literal("Enabled InControl debug mode"));
            } else {
                player.sendSystemMessage(Component.literal("Disabled InControl debug mode"));
            }
        }
        return 0;
    }
}

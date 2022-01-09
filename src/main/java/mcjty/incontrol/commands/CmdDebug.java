package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ForgeEventHandlers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;

public class CmdDebug implements Command<CommandSourceStack> {

    private static final CmdDebug CMD = new CmdDebug();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player != null) {
            ForgeEventHandlers.debug = !ForgeEventHandlers.debug;
            if (ForgeEventHandlers.debug) {
                player.sendMessage(new TextComponent("Enabled InControl debug mode"), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("Disabled InControl debug mode"), Util.NIL_UUID);
            }
        }
        return 0;
    }
}

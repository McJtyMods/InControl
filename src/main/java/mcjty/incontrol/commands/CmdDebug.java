package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ForgeEventHandlers;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class CmdDebug implements Command<CommandSource> {

    private static final CmdDebug CMD = new CmdDebug();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        if (player != null) {
            ForgeEventHandlers.debug = !ForgeEventHandlers.debug;
            if (ForgeEventHandlers.debug) {
                player.sendMessage(new StringTextComponent("Enabled InControl debug mode"), Util.DUMMY_UUID);
            } else {
                player.sendMessage(new StringTextComponent("Disabled InControl debug mode"), Util.DUMMY_UUID);
            }
        }
        return 0;
    }
}

package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.areas.AreaSystem;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class CmdArea {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("area")
                .requires(cs -> cs.hasPermission(2))
                .executes(CmdArea::showArea);
    }

    private static int showArea(CommandContext<CommandSourceStack> context) {
        BlockPos pos = context.getSource().getPlayer().blockPosition();
        String area = AreaSystem.isInArea(context.getSource().getPlayer().level(), pos.getX(), pos.getY(), pos.getZ());
        if (area == null) {
            context.getSource().sendSuccess(() -> Component.literal("Not in any area"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Current area: " + area), false);
        }
        return 0;
    }
}

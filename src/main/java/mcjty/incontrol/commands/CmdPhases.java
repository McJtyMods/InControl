package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class CmdPhases {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("phases")
                .requires(cs -> cs.hasPermission(2))
                .executes(CmdPhases::showPhases);
    }

    private static int showPhases(CommandContext<CommandSourceStack> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        String phases = "";
        for (String phase : data.getPhases()) {
            phases += phase + " ";
        }
        context.getSource().sendSuccess(new TextComponent("Current phases: " + phases), false);
        return 0;
    }
}

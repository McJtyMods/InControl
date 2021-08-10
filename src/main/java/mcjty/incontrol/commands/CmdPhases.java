package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CmdPhases {

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("phases")
                .requires(cs -> cs.hasPermission(2))
                .executes(CmdPhases::showPhases);
    }

    private static int showPhases(CommandContext<CommandSource> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        String phases = "";
        for (String phase : data.getPhases()) {
            phases += phase + " ";
        }
        context.getSource().sendSuccess(new StringTextComponent("Current phases: " + phases), false);
        return 0;
    }
}

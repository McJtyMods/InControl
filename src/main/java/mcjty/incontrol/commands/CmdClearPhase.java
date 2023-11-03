package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdClearPhase {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("clearphase")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("phase", StringArgumentType.word())
                        .executes(CmdClearPhase::clearPhase));
    }

    private static int clearPhase(CommandContext<CommandSourceStack> context) {
        String phase = context.getArgument("phase", String.class);
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        data.setPhase(phase, false);
        return 0;
    }
}

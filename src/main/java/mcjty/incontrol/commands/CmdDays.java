package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CmdDays {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("days")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("number", IntegerArgumentType.integer()).executes(CmdDays::setDays))
                .executes(CmdDays::showDays);
    }

    private static int showDays(CommandContext<CommandSourceStack> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        context.getSource().sendSuccess(() -> Component.literal("Current day is " + data.getDaycounter()), false);
        return 0;
    }

    private static int setDays(CommandContext<CommandSourceStack> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        Integer number = context.getArgument("number", Integer.class);
        data.setDaycounter(number);
        return 0;
    }

}

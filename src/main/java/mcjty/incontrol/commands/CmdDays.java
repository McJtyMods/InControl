package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CmdDays {

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("days")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("number", IntegerArgumentType.integer()).executes(CmdDays::setDays))
                .executes(CmdDays::showDays);
    }

    private static int showDays(CommandContext<CommandSource> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        context.getSource().sendSuccess(new StringTextComponent("Current day is " + data.getDaycounter()), false);
        return 0;
    }

    private static int setDays(CommandContext<CommandSource> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        Integer number = context.getArgument("number", Integer.class);
        data.setDaycounter(number);
        return 0;
    }

}

package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdSetNumber {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("setnumber")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("number", StringArgumentType.word())
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes(CmdSetNumber::setNumber)));
    }

    private static int setNumber(CommandContext<CommandSourceStack> context) {
        String number = context.getArgument("number", String.class);
        int value = context.getArgument("value", Integer.class);
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        data.setNumber(number, value);
        return 0;
    }
}

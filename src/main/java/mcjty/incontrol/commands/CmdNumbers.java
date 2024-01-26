package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcjty.incontrol.data.DataStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class CmdNumbers {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("numbers")
                .requires(cs -> cs.hasPermission(2))
                .executes(CmdNumbers::showNumbers);
    }

    private static int showNumbers(CommandContext<CommandSourceStack> context) {
        DataStorage data = DataStorage.getData(context.getSource().getLevel());
        Map<String, Integer> numbers = data.getNumbers();
        context.getSource().sendSuccess(() -> Component.literal("Current numbers: " + numbers), false);
        return 0;
    }
}

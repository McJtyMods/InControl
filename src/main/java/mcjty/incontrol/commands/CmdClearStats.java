package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.data.Statistics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdClearStats implements Command<CommandSourceStack> {

    private static final CmdClearStats CMD = new CmdClearStats();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("clearstats").requires(cs -> cs.hasPermission(0)).executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Statistics.clear();
        return 0;
    }
}

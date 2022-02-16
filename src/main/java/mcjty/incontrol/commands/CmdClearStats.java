package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.data.Statistics;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CmdClearStats implements Command<CommandSource> {

    private static final CmdClearStats CMD = new CmdClearStats();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("clearstats")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Statistics.clear();
        return 0;
    }
}

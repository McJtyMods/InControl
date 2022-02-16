package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.data.Statistics;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CmdShowStats implements Command<CommandSource> {

    private static final CmdShowStats CMD = new CmdShowStats();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("showstats")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Statistics.dump();
        return 0;
    }
}

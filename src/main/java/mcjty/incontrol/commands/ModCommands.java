package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.incontrol.InControl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(InControl.MODID)
                        .then(CmdDebug.register(dispatcher))
                        .then(CmdKillMobs.register(dispatcher))
                        .then(CmdReload.register(dispatcher))
                        .then(CmdShowMobs.register(dispatcher))
                        .then(CmdList.register(dispatcher))
                        .then(CmdInfo.register(dispatcher))
                        .then(CmdDays.register(dispatcher))
                        .then(CmdPhases.register(dispatcher))
        );

        dispatcher.register(Commands.literal("ctrl").redirect(commands));
    }

}

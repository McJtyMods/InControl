package mcjty.incontrol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.incontrol.InControl;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> commands = dispatcher.register(
                Commands.literal(InControl.MODID)
                        .then(CmdDebug.register(dispatcher))
                        .then(CmdKillMobs.register(dispatcher))
                        .then(CmdReload.register(dispatcher))
                        .then(CmdShowMobs.register(dispatcher))
                        .then(CmdInfo.register(dispatcher))
        );

        dispatcher.register(Commands.literal("ctrl").redirect(commands));
    }

}

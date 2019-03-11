package mcjty.incontrol.commands;

import mcjty.incontrol.ForgeEventHandlers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdDebug extends CommandBase {
    @Override
    public String getName() {
        return "ctrldebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrldebug";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ForgeEventHandlers.debug = !ForgeEventHandlers.debug;
        if (ForgeEventHandlers.debug) {
            sender.sendMessage(new TextComponentString("Enabled InControl debug mode"));
        } else {
            sender.sendMessage(new TextComponentString("Disabled InControl debug mode"));
        }
    }
}

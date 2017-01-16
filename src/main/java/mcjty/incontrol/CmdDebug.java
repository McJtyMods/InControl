package mcjty.incontrol;

import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdDebug extends CompatCommandBase {
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
            ChatTools.addChatMessage(sender, new TextComponentString("Enabled InControl debug mode"));
        } else {
            ChatTools.addChatMessage(sender, new TextComponentString("Disabled InControl debug mode"));
        }
    }
}

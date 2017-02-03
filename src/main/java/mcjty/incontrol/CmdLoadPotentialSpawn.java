package mcjty.incontrol;

import mcjty.incontrol.rules.RulesManager;
import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdLoadPotentialSpawn extends CompatCommandBase {
    @Override
    public String getName() {
        return "ctrlloadpotentialspawn";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrlloadpotentialspawn";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!RulesManager.readCustomPotentialSpawn(args[0])) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Error reading file '" + args[0] + "'!"));
            InControl.logger.warn("Error reading file '" + args[0] + "'!");
        }
    }
}

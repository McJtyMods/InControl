package mcjty.incontrol;

import mcjty.incontrol.rules.SpawnRules;
import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdReload extends CompatCommandBase {
    @Override
    public String getName() {
        return "ctrlreload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrlreload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ChatTools.addChatMessage(sender, new TextComponentString("Reloaded InControl rules"));
        SpawnRules.reloadRules();
    }
}

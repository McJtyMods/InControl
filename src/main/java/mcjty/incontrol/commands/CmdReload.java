package mcjty.incontrol.commands;

import mcjty.incontrol.rules.RulesManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdReload extends CommandBase {
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
        sender.sendMessage(new TextComponentString("Reloaded InControl rules"));
        RulesManager.reloadRules();
    }
}

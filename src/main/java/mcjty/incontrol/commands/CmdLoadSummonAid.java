package mcjty.incontrol.commands;

import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdLoadSummonAid extends CommandBase {
    @Override
    public String getName() {
        return "ctrlloadsummonaid";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrlloadsummonaid";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!RulesManager.readCustomSummonAid(args[0])) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error reading file '" + args[0] + "'!"));
            InControl.setup.getLogger().warn("Error reading file '" + args[0] + "'!");
        }
    }
}

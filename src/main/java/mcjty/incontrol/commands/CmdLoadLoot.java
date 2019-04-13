package mcjty.incontrol.commands;

import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class CmdLoadLoot extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "ctrlloadloot";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "ctrlloadloot";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        if (!RulesManager.readCustomLoot(args[0])) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error reading file '" + args[0] + "'!"));
            InControl.setup.getLogger().warn("Error reading file '" + args[0] + "'!");
        }
    }
}

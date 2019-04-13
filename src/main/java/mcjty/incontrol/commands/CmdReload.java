package mcjty.incontrol.commands;

import mcjty.incontrol.rules.RulesManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CmdReload extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "ctrlreload";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "ctrlreload";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, ICommandSender sender, @Nonnull String[] args) {
        sender.sendMessage(new TextComponentString("Reloaded InControl rules"));
        RulesManager.reloadRules();
    }
}

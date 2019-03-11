package mcjty.incontrol.commands;

import mcjty.incontrol.InControl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.Set;

public class CmdShowMobs extends CommandBase {
    @Override
    public String getName() {
        return "ctrlshowmobs";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrlshowmobs";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Set<ResourceLocation> keys = ForgeRegistries.ENTITIES.getKeys();
        keys.forEach(s -> InControl.setup.getLogger().log(Level.INFO, "Mob:" + s));
    }
}

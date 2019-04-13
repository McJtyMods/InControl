package mcjty.incontrol.commands;

import mcjty.incontrol.InControl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.Set;

public class CmdShowMobs extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "ctrlshowmobs";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "ctrlshowmobs";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        Set<ResourceLocation> keys = ForgeRegistries.ENTITIES.getKeys();
        keys.forEach(s -> InControl.setup.getLogger().log(Level.INFO, "Mob:" + s));
    }
}

package mcjty.incontrol;

import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.EntityTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;

public class CmdShowMobs extends CompatCommandBase {
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
        EntityTools.getEntities().forEach(s -> InControl.logger.log(Level.INFO, "Mob:" + s));
    }
}

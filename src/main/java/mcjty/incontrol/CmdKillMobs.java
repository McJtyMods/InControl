package mcjty.incontrol;

import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.EntityTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.util.List;

public class CmdKillMobs extends CompatCommandBase {
    @Override
    public String getName() {
        return "ctrlkill";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrlkill";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Use 'all', 'passive', 'hostile' or name of the mob followed by optional dimension id"));
            InControl.logger.error("Use 'all', 'passive', 'hostile', 'entity' or name of the mob followed by optional dimension id");
            return;
        }
        int dimension = (sender instanceof EntityPlayer) ? sender.getEntityWorld().provider.getDimension() : 0;
        if (args.length > 1) {
            dimension = Integer.parseInt(args[1]);
        }
        String arg0 = args[0].toLowerCase();
        boolean all = "all".equals(arg0);
        boolean passive = "passive".equals(arg0);
        boolean hostile = "hostile".equals(arg0);
        boolean entity = "entity".equals(arg0);

        WorldServer worldServer = server.worldServerForDimension(dimension);
        List<Entity> entities = worldServer.getEntities(Entity.class, input -> {
            if (all) {
                return !(input instanceof EntityPlayer);
            } else if (passive) {
                return input instanceof IAnimals && !(input instanceof IMob);
            } else if (hostile) {
                return input instanceof IMob;
            } else if (entity) {
                return !(input instanceof IAnimals) && !(input instanceof EntityPlayer);
            } else {
                String id = EntityTools.findEntityIdByClass(input.getClass());
                return arg0.equals(id);
            }
        });
        for (Entity e : entities) {
            worldServer.removeEntity(e);
        }
        ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.YELLOW + "Removed " + entities.size() + " entities!"));
    }
}

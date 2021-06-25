package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.InControl;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class CmdKillMobs  implements Command<CommandSource> {

    private static final CmdKillMobs CMD = new CmdKillMobs();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("kill")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        if (player != null) {
            String type = context.getArgument("type", String.class);
            if (type == null || type.trim().isEmpty()) {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "Use 'all', 'passive', 'hostile' or name of the mob followed by optional dimension id"), Util.NIL_UUID);
                InControl.setup.getLogger().error("Use 'all', 'passive', 'hostile', 'entity' or name of the mob followed by optional dimension id");
                return 0;
            }
            RegistryKey<World> dimension = player.getCommandSenderWorld().dimension();
//            if (args.length > 1) {
//                dimension = Integer.parseInt(args[1]);
//            }
            boolean all = "all".equals(type);
            boolean passive = "passive".equals(type);
            boolean hostile = "hostile".equals(type);
            boolean entity = "entity".equals(type);

            ServerWorld worldServer = player.getCommandSenderWorld().getServer().getLevel(dimension);
            List<Entity> entities = worldServer.getEntities(null, input -> {
                if (all) {
                    return !(input instanceof PlayerEntity);
                } else if (passive) {
                    return input instanceof AnimalEntity && !(input instanceof IMob);
                } else if (hostile) {
                    return input instanceof IMob;
                } else if (entity) {
                    return !(input instanceof AnimalEntity) && !(input instanceof PlayerEntity);
                } else {
                    String id = input.getType().getRegistryName().toString();
                    return type.equals(id);
                }
            });
            for (Entity e : entities) {
                worldServer.despawn(e);
            }
            player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Removed " + entities.size() + " entities!"), Util.NIL_UUID);
        }
        return 0;
    }
}

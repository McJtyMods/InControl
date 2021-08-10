package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public class CmdList implements Command<CommandSource> {

    private static final CmdList CMD = new CmdList();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("list")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        if (player != null) {
            RegistryKey<World> dimension = player.getCommandSenderWorld().dimension();

            ServerWorld worldServer = player.getCommandSenderWorld().getServer().getLevel(dimension);
            Counter<ResourceLocation> counter = new Counter<>();
            worldServer.getEntities(null, e -> e instanceof MobEntity).forEach(input -> {
                counter.add(input.getType().getRegistryName());
            });
            for (Map.Entry<ResourceLocation, Integer> entry : counter.getMap().entrySet()) {
                player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Mob " + entry.getKey().toString() + ": " + entry.getValue()), Util.NIL_UUID);
            }
        }
        return 0;
    }
}

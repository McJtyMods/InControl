package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class CmdList implements Command<CommandSourceStack> {

    private static final CmdList CMD = new CmdList();

    public static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<>() {
        @Override
        public Entity tryCast(Entity entity) {
            return entity;
        }
        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };


    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("list")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player != null) {
            ResourceKey<Level> dimension = player.getCommandSenderWorld().dimension();

            ServerLevel worldServer = player.getCommandSenderWorld().getServer().getLevel(dimension);
            Counter<ResourceLocation> counter = new Counter<>();
            worldServer.getEntities(ANY_TYPE, e -> e instanceof Mob).forEach(input -> {
                counter.add(ForgeRegistries.ENTITY_TYPES.getKey(input.getType()));
            });
            for (Map.Entry<ResourceLocation, Integer> entry : counter.getMap().entrySet()) {
                player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "Mob " + entry.getKey().toString() + ": " + entry.getValue()));
            }
        }
        return 0;
    }
}

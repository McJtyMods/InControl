package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongSet;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Map;

public class CmdInfo implements Command<CommandSourceStack> {

    private static final CmdInfo CMD = new CmdInfo();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("info")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();
        ServerLevel sw = Tools.getServerWorld(player.level());
        ChunkAccess chunk = sw.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, false);
        if (chunk != null) {
            Map<Structure, LongSet> references = chunk.getAllReferences();
            for (Structure s : references.keySet()) {
                LongSet longs = references.get(s);
                ResourceLocation key = sw.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(s);
                player.sendSystemMessage(Component.literal(key.toString() + ": " + longs.size()));
            }
        }
        int light = sw.getBrightness(LightLayer.BLOCK, pos);
        int light_sky = sw.getBrightness(LightLayer.SKY, pos);
        int light_full = sw.getMaxLocalRawBrightness(pos);
        player.sendSystemMessage(Component.literal("Light: " + light + ", sky: " + light_sky + ", full: " + light_full));
        DataStorage data = DataStorage.getData(sw);
        player.sendSystemMessage(Component.literal("Current day is " + data.getDaycounter()));
        long time = sw.getDayTime();
        player.sendSystemMessage(Component.literal("Current time is " + time + " (daytime: " + (time % 24000) + ")"));
        return 0;
    }
}

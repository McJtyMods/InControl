package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongSet;
import mcjty.tools.varia.Tools;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public class CmdInfo implements Command<CommandSource> {

    private static final CmdInfo CMD = new CmdInfo();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("info")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        if (player != null) {
            BlockPos pos = player.getPosition();
            ServerWorld sw = Tools.getServerWorld(player.world);
            IChunk chunk = sw.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, false);
            if (chunk != null) {
                Map<Structure<?>, LongSet> references = chunk.getStructureReferences();
                for (Structure<?> s : references.keySet()) {
                    LongSet longs = references.get(s);
                    player.sendMessage(new StringTextComponent(s.getRegistryName().toString() + ": " + longs.size()), Util.DUMMY_UUID);
                }
            }
        }
        return 0;
    }
}

package mcjty.incontrol.tools.distance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;


/**
 * Tracks which players are within a given chunk radius of every chunk in the world.
 * This is a direct port of the player distance map from Paper's 'per-player mob spawns' (https://github.com/PaperMC/Paper/pull/2171).
 */
public final class PlayerDistanceMap {
    private static final PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer> EMPTY_SET = new PooledHashSets.PooledObjectLinkedOpenHashSet<>();

    // Map of each tracked player to they last known ChunkPos
    private final Map<ServerPlayer, ChunkPos> players = new HashMap<>();

    // Maps each chunk to the pooled set of players within range of it, sinchronized for thread safety.
    private final Long2ObjectOpenHashMap<PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer>> playerMapUnsync = new Long2ObjectOpenHashMap<>(1024, 0.5f);
    private final Long2ObjectMap<PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer>> playerMap = Long2ObjectMaps.synchronize(playerMapUnsync);

    // Chunk radius
    private int viewDistance;

    // Pool of deduplicated sets
    private final PooledHashSets<ServerPlayer> pooledHashSets = new PooledHashSets<>();

    // Gets players within range of a given chunk or empty (given chunk key)
    public PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer> getPlayersInRange(final long chunkKey) {
        return this.playerMap.getOrDefault(chunkKey, EMPTY_SET);
    }

    // Gets players within range of a given chunk or empty (given chunk pos)
    public PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer> getPlayersInRange(final ChunkPos pos) {
        return getPlayersInRange(pos.toLong());
    }

    // Called every tick with the current player list and view distance.
    // - Figures out which players have left.
    // - For each current player, compares their new chunk position to their old one.
    // - Calls addNewPlayer, updatePlayer, or removePlayer as appropriate.
    // - Cleans up players that are no longer online.
    public void update(final List<ServerPlayer> currentPlayers, final int newViewDistance) {
        final ObjectLinkedOpenHashSet<ServerPlayer> gone = new ObjectLinkedOpenHashSet<>(this.players.keySet());

        final int oldViewDistance = this.viewDistance;
        this.viewDistance = newViewDistance;

        for (final ServerPlayer player : currentPlayers) {
            if (player.isSpectator())
                continue;

            gone.remove(player);

            final ChunkPos newPosition = player.chunkPosition();
            final ChunkPos oldPosition = this.players.put(player, newPosition);

            if (oldPosition == null) {
                addNewPlayer(player, newPosition, newViewDistance);
            } else {
                updatePlayer(player, oldPosition, newPosition, oldViewDistance, newViewDistance);
            }
        }

        for (final ServerPlayer player : gone) {
            final ChunkPos oldPosition = this.players.remove(player);
            if (oldPosition != null) {
                removePlayer(player, oldPosition, oldViewDistance);
            }
        }
    }

    // Adds a player to a chunk's player set
    private void addPlayerTo(final ServerPlayer player, final int chunkX, final int chunkZ) {
        this.playerMap.compute(ChunkPos.asLong(chunkX, chunkZ), (key, set) -> set == null
                ? new PooledHashSets.PooledObjectLinkedOpenHashSet<>(player)
                : this.pooledHashSets.findMapWith(set, player));
    }

    // Guess
    private void removePlayerFrom(final ServerPlayer player, final int chunkX, final int chunkZ) {
        this.playerMap.compute(ChunkPos.asLong(chunkX, chunkZ), (key, set) -> {
            if (set == null)
                return null;
            return this.pooledHashSets.findMapWithout(set, player);
        });
    }

    // Instead of removing the player from all old chunks and re-adding to all new chunks,
    // we compute only the chunks that were added to the view and the chunks that fell out of it. 
    // We use the direction of movement to iterate only the newly covered and newly uncovered strips of chunks.
    // If the movement is large enough that the old and new view areas don't overlap, it falls back to a full remove + add.
    private void updatePlayer(final ServerPlayer player, final ChunkPos oldPosition, final ChunkPos newPosition, final int oldViewDistance, final int newViewDistance) {
        final int toX = newPosition.x;
        final int toZ = newPosition.z;
        final int fromX = oldPosition.x;
        final int fromZ = oldPosition.z;

        final int dx = toX - fromX;
        final int dz = toZ - fromZ;

        if (Math.max(Math.abs(dx), Math.abs(dz)) >= (2 * oldViewDistance) || oldViewDistance != newViewDistance) {
            removePlayer(player, oldPosition, oldViewDistance);
            addNewPlayer(player, newPosition, newViewDistance);
            return;
        }

        final int up = 1 | (dz >> (Integer.SIZE - 1));
        final int right = 1 | (dx >> (Integer.SIZE - 1));

        int maxX, minX, maxZ, minZ;

        if (dx != 0) {
            maxX = toX + (oldViewDistance * right) + right;
            minX = fromX + (oldViewDistance * right) + right;
            maxZ = fromZ + (oldViewDistance * up) + up;
            minZ = toZ - (oldViewDistance * up);
            for (int x = minX; x != maxX; x += right)
                for (int z = minZ; z != maxZ; z += up)
                    addPlayerTo(player, x, z);
        }
        if (dz != 0) {
            maxX = toX + (oldViewDistance * right) + right;
            minX = toX - (oldViewDistance * right);
            maxZ = toZ + (oldViewDistance * up) + up;
            minZ = fromZ + (oldViewDistance * up) + up;
            for (int x = minX; x != maxX; x += right)
                for (int z = minZ; z != maxZ; z += up)
                    addPlayerTo(player, x, z);
        }
        if (dx != 0) {
            maxX = toX - (oldViewDistance * right);
            minX = fromX - (oldViewDistance * right);
            maxZ = fromZ + (oldViewDistance * up) + up;
            minZ = toZ - (oldViewDistance * up);
            for (int x = minX; x != maxX; x += right)
                for (int z = minZ; z != maxZ; z += up)
                    removePlayerFrom(player, x, z);
        }
        if (dz != 0) {
            maxX = fromX + (oldViewDistance * right) + right;
            minX = fromX - (oldViewDistance * right);
            maxZ = toZ - (oldViewDistance * up);
            minZ = fromZ - (oldViewDistance * up);
            for (int x = minX; x != maxX; x += right)
                for (int z = minZ; z != maxZ; z += up)
                    removePlayerFrom(player, x, z);
        }
    }

    // Registers a player across all chunks in their view distance square.
    // (2 * viewDistance + 1) by (2 * viewDistance + 1) grid centered on the player.
    private void addNewPlayer(final ServerPlayer player, final ChunkPos position, final int viewDistance) {
        final int x = position.x;
        final int z = position.z;
        for (int xoff = -viewDistance; xoff <= viewDistance; ++xoff)
            for (int zoff = -viewDistance; zoff <= viewDistance; ++zoff)
                addPlayerTo(player, x + xoff, z + zoff);
    }

    // Unregisters a player from all chunks in their view distance square.
    private void removePlayer(final ServerPlayer player, final ChunkPos position, final int viewDistance) {
        final int x = position.x;
        final int z = position.z;
        for (int xoff = -viewDistance; xoff <= viewDistance; ++xoff)
            for (int zoff = -viewDistance; zoff <= viewDistance; ++zoff)
                removePlayerFrom(player, x + xoff, z + zoff);
    }
}
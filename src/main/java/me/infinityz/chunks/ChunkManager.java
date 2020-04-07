package me.infinityz.chunks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.types.ClaimableChunk;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.players.SurvivalPlayer;

/**
 * ChunkManager
 */
public class ChunkManager {
    SurvivalUniverse instance;
    public Map<IChunk, UUID> ownedChunksMap;
    public Map<ClaimableChunk, Integer> claimableChunks;

    public ChunkManager(SurvivalUniverse instance) {
        this.instance = instance;
        this.ownedChunksMap = new HashMap<>();
        this.claimableChunks = new HashMap<>();
    }

    public IChunk findIChunkfromChunk(Chunk chunk) {
        return ownedChunksMap.keySet().parallelStream().filter(it -> compareIchunkChunk(chunk, it)).findFirst()
                .orElse(null);
    }

    public ClaimableChunk findClaimableChunkFromChunk(Chunk chunk) {
        return claimableChunks.keySet().parallelStream().filter(it -> compareIchunkChunk(chunk, it)).findFirst()
                .orElse(null);
    }

    public boolean addClaimableChunk(ClaimableChunk cu) {
        if (!verifyDups(cu)) {
            claimableChunks.put(cu, 0);
            return true;
        }
        return false;
    }

    public boolean addClaimableChunk(ClaimableChunk cu, boolean addToFile) {
        if (!verifyDups(cu)) {
            claimableChunks.put(cu, 0);
            final List<String> st = instance.claimableChunkFile.getStringList("claimable-chunk-list");
            st.add(cu.chunkX + " " + cu.chunkZ + " " + cu.chunkWorld.getName());
            instance.claimableChunkFile.set("claimable-chunk-list", st);
            instance.claimableChunkFile.save();
            instance.claimableChunkFile.reload();

            return true;
        }
        return false;
    }

    public ClaimableChunk getNearestClaimableChunk(Location loc) {
        /** Ensure the list of available chunks is not empty */
        if (claimableChunks.isEmpty())
            return null;

        /** Return this value later, in the mean time keep it as null */
        ClaimableChunk claimableChunk = null;
        /** Obtain the current x and z. Mathematically you would use x and y. */
        final int x1 = loc.getChunk().getX();
        final int z1 = loc.getChunk().getZ();
        /*
         * Set the shortest distance to the longest possible distance to allow
         * everyhting in the loop to work
         */
        double shortest_distance = Double.MAX_VALUE;
        /**
         * Obtain the iterator to use a while loop. For loops work just fine but I
         * prefer while
         */
        final Iterator<ClaimableChunk> iter = claimableChunks.keySet().iterator();
        /** Check if the iterator has next on each iteration */
        while (iter.hasNext()) {
            /** Obtain the next object */
            final ClaimableChunk c = iter.next();
            /** Obtain your x2 and z2 */
            final int x2 = c.chunkX;
            final int z2 = c.chunkZ;
            /** Clasical squared distance */
            final double distance = Math.abs(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2)));

            /**
             * Check if the current shortest distance is greater than the calculated
             * distance and set it as the new shortest if needed
             */
            if (shortest_distance >= distance) {
                shortest_distance = distance;
                claimableChunk = c;
            }
        }
        /** Finally return, might be null */
        return claimableChunk;
    }

    public PlayerChunk getNearestPlayerChunk(Location loc, Player su) {
        /**Obtain a list of the player's chunks */
        final List<IChunk> player_Chunks = ownedChunksMap.keySet().parallelStream().filter(it -> it instanceof PlayerChunk).filter(
                it -> ((PlayerChunk) it).owner.getMostSignificantBits() == su.getUniqueId().getMostSignificantBits())
                .collect(Collectors.toCollection(ArrayList::new));

        if(player_Chunks.isEmpty())return null;
        /** Return this value later, in the mean time keep it as null */
        IChunk chunk = null;
        /** Obtain the current x and z. Mathematically you would use x and y. */
        final int x1 = loc.getChunk().getX();
        final int z1 = loc.getChunk().getZ();
        /*
         * Set the shortest distance to the longest possible distance to allow
         * everyhting in the loop to work
         */
        double shortest_distance = Double.MAX_VALUE;
        /**
         * Obtain the iterator to use a while loop. For loops work just fine but I
         * prefer while
         */
        final Iterator<IChunk> iter = player_Chunks.iterator();
        /** Check if the iterator has next on each iteration */
        while (iter.hasNext()) {
            /** Obtain the next object */
            final IChunk c = iter.next();
            /** Obtain your x2 and z2 */
            final int x2 = c.chunkX;
            final int z2 = c.chunkZ;
            /** Clasical squared distance */
            final double distance = Math.abs(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2)));

            /**
             * Check if the current shortest distance is greater than the calculated
             * distance and set it as the new shortest if needed
             */
            if (shortest_distance >= distance) {
                shortest_distance = distance;
                chunk = c;
            }
        }

        return (PlayerChunk)chunk;
    }

    public boolean removeClaimableChunk(ClaimableChunk cu, boolean addToFile) {
        final List<String> st = instance.claimableChunkFile.getStringList("claimable-chunk-list");
        st.remove(cu.chunkX + " " + cu.chunkZ + " " + cu.chunkWorld.getName());
        instance.claimableChunkFile.set("claimable-chunk-list", st);
        instance.claimableChunkFile.save();
        instance.claimableChunkFile.reload();
        return claimableChunks.remove(cu, 0);
    }

    public Location getCenterLocationChunk(Chunk c) {
        Location loc = c.getBlock(7, 255, 7).getLocation();
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
        return loc;
    }

    public Location getCenterLocationChunk(IChunk c) {
        return c != null && c.chunkWorld != null ? getCenterLocationChunk(c.chunkWorld.getChunkAt(c.chunkX, c.chunkZ)) : null;
    }

    public PlayerChunk claimChunk(SurvivalPlayer su, ClaimableChunk cu) {
        if (cu != null && su != null) {
            final PlayerChunk newIChunk = new PlayerChunk(su.playerUUID, cu.chunkWorld.getName(), cu.chunkX, cu.chunkZ);
            if (!verifyDups(newIChunk)) {
                if (claimableChunks.remove(cu) != null) {
                    ownedChunksMap.put(newIChunk, su.playerUUID);
                    final String keyString = "chunks." + su.playerUUID + "." + newIChunk.toString();
                    instance.chunksFile.set(keyString + ".world", newIChunk.chunkWorld.getName());
                    instance.chunksFile.set(keyString + ".x-coordinate", newIChunk.chunkX);
                    instance.chunksFile.set(keyString + ".z-coordinate", newIChunk.chunkZ);
                    instance.chunksFile.save();
                    instance.chunksFile.reload();
                    final List<String> st = instance.claimableChunkFile.getStringList("claimable-chunk-list");
                    st.remove(cu.chunkX + " " + cu.chunkZ + " " + cu.chunkWorld.getName());
                    instance.claimableChunkFile.set("claimable-chunk-list", st);
                    instance.claimableChunkFile.save();
                    instance.claimableChunkFile.reload();
                    return newIChunk;

                }
            } else {
                final List<String> st = instance.claimableChunkFile.getStringList("claimable-chunk-list");
                st.remove(cu.chunkX + " " + cu.chunkZ + " " + cu.chunkWorld.getName());
                instance.claimableChunkFile.set("claimable-chunk-list", st);
                instance.claimableChunkFile.save();
                instance.claimableChunkFile.reload();
                claimableChunks.remove(cu);
                Bukkit.broadcastMessage("No can't do.");
            }

        }
        return null;
    }

    public IChunk getChunkNoType(Chunk chunk) {
        return findClaimableChunkFromChunk(chunk) != null ? findClaimableChunkFromChunk(chunk)
                : findIChunkfromChunk(chunk);
    }

    public boolean verifyDups(IChunk ichunk) {
        return ownedChunksMap.keySet().stream().filter(it -> compareIChunkIChunk(ichunk, it)).findFirst().isPresent();
    }

    boolean compareIChunkIChunk(IChunk c1, IChunk c2) {
        return c1.chunkX == c2.chunkX && c1.chunkZ == c2.chunkZ && c1.chunkWorld == c2.chunkWorld;
    }

    boolean compareIchunkChunk(Chunk chunk, IChunk iChunk) {
        return chunk.getX() == iChunk.chunkX && chunk.getZ() == iChunk.chunkZ && chunk.getWorld() == iChunk.chunkWorld;
    }

}
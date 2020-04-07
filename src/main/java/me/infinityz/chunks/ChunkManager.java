package me.infinityz.chunks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

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
    public boolean removeClaimableChunk(ClaimableChunk cu, boolean addToFile) {
            final List<String> st = instance.claimableChunkFile.getStringList("claimable-chunk-list");
            st.remove(cu.chunkX + " " + cu.chunkZ + " " + cu.chunkWorld.getName());
            instance.claimableChunkFile.set("claimable-chunk-list", st);
            instance.claimableChunkFile.save();
            instance.claimableChunkFile.reload();
        return  claimableChunks.remove(cu, 0);
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
package me.infinityz.chunks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;

import me.infinityz.SurvivalUniverse;

/**
 * ChunkManager
 */
public class ChunkManager {
    SurvivalUniverse instance;
    public Map<IChunk, UUID> ownedChunksMap;

    public ChunkManager(SurvivalUniverse instance) {
        this.instance = instance;
        this.ownedChunksMap = new HashMap<>();
    }

    public IChunk findIChunkfromChunk(Chunk chunk) {
        for (IChunk ichunk : ownedChunksMap.keySet())
            if (compareIchunkChunk(chunk, ichunk))
                return ichunk;

        return null;
    }

    public boolean compareIchunkChunk(Chunk chunk, IChunk iChunk) {
        return chunk.getX() == iChunk.chunkX && chunk.getZ() == iChunk.chunkZ && chunk.getWorld() == iChunk.chunkWorld;
    }

}
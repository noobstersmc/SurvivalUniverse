package me.infinityz.chunks.types;

import java.util.UUID;

import me.infinityz.chunks.ChunkType;
import me.infinityz.chunks.IChunk;

/**
 * PlayerChunk
 */
public class PlayerChunk  extends IChunk {
    public ChunkType chunkType = ChunkType.PLAYER;
    public UUID owner;
    public String owner_last_known_name;
    
}
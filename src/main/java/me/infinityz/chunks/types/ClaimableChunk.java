package me.infinityz.chunks.types;

import org.bukkit.Bukkit;

import me.infinityz.chunks.IChunk;

public class ClaimableChunk extends IChunk{

    public ClaimableChunk(int x, int z, String world){
        super();
        this.chunkX = x;
        this.chunkZ = z;
        this.chunkWorld = Bukkit.getWorld(world);
        }

}
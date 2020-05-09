package me.infinityz.chunks.types;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.infinityz.chunks.IChunk;

public class ClaimableChunk extends IChunk {

    public ClaimableChunk(int x, int z, String world) {
        super(x, z, Bukkit.getWorld(world));
    }

    public ClaimableChunk(int x, int z, World world) {
        super(x, z, world);
    }

}
package me.infinityz.chunks.types;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.infinityz.chunks.ChunkType;
import me.infinityz.chunks.IChunk;

/**
 * PlayerChunk
 */
public class PlayerChunk  extends IChunk {
    public ChunkType chunkType = ChunkType.PLAYER;
    public UUID owner;
    public String owner_last_known_name;

    public PlayerChunk(UUID owner, String world, int x, int z){
        this.chunkX = x;
        this.chunkZ = z;
        this.chunkWorld = Bukkit.getWorld(world);
        this.owner = owner;
        this.owner_last_known_name = Bukkit.getOfflinePlayer(owner).getName();
    }

    public boolean isOwner(Player player){
        return owner.getMostSignificantBits() == player.getUniqueId().getMostSignificantBits();
    }
    

    @Override
    public String toString(){
        return owner_last_known_name + " (" + chunkX + ", " + chunkZ + ") (" + chunkWorld.getName() + ")";
    }
    
}
package me.infinityz.players;

import java.util.UUID;

import me.infinityz.chunks.types.PlayerChunk;

/**
 * SurvivalPlayer
 */
public class SurvivalPlayer {
    public UUID playerUUID;
    public UUID[] allies;
    public PlayerChunk[] playerChunks;
    public boolean pvp = false;

    public SurvivalPlayer(UUID uuid){
        this.playerUUID = uuid;
        //Call the database and obtain allies.
    }
    
}
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

    public SurvivalPlayer(UUID uuid) {
        this.playerUUID = uuid;
        // Call the database and obtain allies.
    }

    public boolean isAlly(UUID uuid) {
        if (allies == null || allies.length == 0)
            return false;
        for (UUID id : allies)
            if (id.getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        return false;
    }

}
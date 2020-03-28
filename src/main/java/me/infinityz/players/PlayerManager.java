package me.infinityz.players;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.infinityz.SurvivalUniverse;

/**
 * PlayerManager
 */
public class PlayerManager {
    public Map<UUID, SurvivalPlayer> survivalPlayerMap;
    SurvivalUniverse instance;

    public PlayerManager(SurvivalUniverse instance){
        this.instance = instance;
        survivalPlayerMap = new HashMap<>();
    }

    public SurvivalPlayer getPlayerFromId(UUID uuid){
        return compareTo(uuid) == null ? new SurvivalPlayer(uuid, 1) : survivalPlayerMap.get(uuid);
    }

    SurvivalPlayer compareTo(UUID id){
        for(SurvivalPlayer pl : survivalPlayerMap.values())if(pl.playerUUID.getMostSignificantBits() == id.getMostSignificantBits())return pl;
        return null;
    }

    
}
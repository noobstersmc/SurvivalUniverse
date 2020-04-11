package me.infinityz.players;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.infinityz.SurvivalUniverse;

/**
 * PlayerManager
 */
public class PlayerManager {
    public Map<UUID, SurvivalPlayer> survivalPlayerMap;
    SurvivalUniverse instance;

    public PlayerManager(SurvivalUniverse instance) {
        this.instance = instance;
        survivalPlayerMap = new HashMap<>();
    }

    public SurvivalPlayer getPlayerFromId(UUID uuid) {
        return survivalPlayerMap.values().stream()
                .filter(it -> it.playerUUID.getMostSignificantBits() == uuid.getMostSignificantBits()).findFirst()
                .orElseGet(() -> new SurvivalPlayer(uuid, 1));
    }

    public String getPlayerNameFromUUID(UUID uuid) throws Exception {
        JSONObject json = (JSONObject) new JSONParser().parse(ConnectionExample.sendGET(uuid));
        return (String) ((JSONObject) ((JSONObject) json.get("data")).get("player")).get("username");
    }

}
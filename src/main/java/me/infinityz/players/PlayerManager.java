package me.infinityz.players;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        final Optional<SurvivalPlayer> option = compareTo(uuid);
        return !option.isPresent() || option == null ? new SurvivalPlayer(uuid, 1) : survivalPlayerMap.get(uuid);
    }

    Optional<SurvivalPlayer> compareTo(UUID id) {
        return survivalPlayerMap.values().stream().filter(it -> it.playerUUID.getMostSignificantBits() == id.getMostSignificantBits()).findFirst();
    }

}
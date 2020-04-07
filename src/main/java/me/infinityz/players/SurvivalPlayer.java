package me.infinityz.players;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.infinityz.SurvivalUniverse;

/**
 * SurvivalPlayer
 */
public class SurvivalPlayer {
    public UUID playerUUID;
    public UUID[] allies = new UUID[0];
    public boolean pvp = false;

    public SurvivalPlayer(UUID uuid) {
        this.playerUUID = uuid;
        // Call the database and obtain allies.
        if (SurvivalUniverse.instance.databaseManager.alliesCachedData.get(uuid) == null) {
            Bukkit.getScheduler().runTaskLater(SurvivalUniverse.instance, () -> {
                if (SurvivalUniverse.instance.databaseManager.alliesCachedData.get(uuid) != null) {
                    this.allies = SurvivalUniverse.instance.databaseManager.alliesCachedData.get(uuid);
                }
            }, 20);
            return;
        }
        this.allies = SurvivalUniverse.instance.databaseManager.alliesCachedData.get(uuid);
    }

    public SurvivalPlayer(UUID uuid, int i) {
        this.playerUUID = uuid;
        SurvivalUniverse.instance.playerManager.survivalPlayerMap.put(uuid, this);
        try {
            SurvivalUniverse.instance.databaseManager.database.loadPlayer(uuid);
            this.allies = SurvivalUniverse.instance.databaseManager.alliesCachedData.get(uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isAlly(UUID uuid, boolean use_legacy) {
        if (allies == null || allies.length == 0 || uuid == null)
            return false;
        for (UUID id : allies) {
            if (id == null || uuid == null)
                continue;
            if (id.getMostSignificantBits() == uuid.getMostSignificantBits()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlly(UUID uuid) {
        return Objects.nonNull(uuid) ? Arrays.asList(allies).stream().filter(Objects::nonNull)
                .filter(it -> uuid.getMostSignificantBits() == it.getMostSignificantBits()).findFirst().isPresent()
                : false;
    }

}
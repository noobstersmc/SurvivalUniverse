package me.infinityz.players;

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

    public boolean isAlly(UUID uuid) {
        if (allies == null || allies.length == 0 || uuid == null)
            return false;
        for (UUID id : allies) {
            if (id.getMostSignificantBits() == uuid.getMostSignificantBits()) {
                return true;
            }
        }
        return false;
    }

    public void addAlly(UUID uuid) {

    }

    public void removeAlly(UUID uuid) {

    }

}
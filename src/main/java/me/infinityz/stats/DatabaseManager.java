package me.infinityz.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.infinityz.SurvivalUniverse;
import me.infinityz.stats.core.MySQL;
import me.infinityz.stats.types.vMySQL;

/**
 * DatabaseManager
 */
public class DatabaseManager {
    SurvivalUniverse instance;
    public IDatabase database;
    public Map<UUID, UUID[]>alliesCachedData;

    public DatabaseManager(SurvivalUniverse instance){
        this.instance = instance;
        this.alliesCachedData = new HashMap<>();
        //TODO: Add support for other databases in the future;
        this.database = new vMySQL(new MySQL("u2944_O65zg0A61L", "jymVRskQ9fFrMT82x7ee2r4Q", "s2944_db", "104.238.205.97"), this);
    }
}
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
        this.database = new vMySQL(new MySQL("root", "p1p2p3p4p5p6", "ciudadela", "155.138.230.169"), this);
    }
}
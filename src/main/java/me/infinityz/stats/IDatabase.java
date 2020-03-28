package me.infinityz.stats;

import java.util.UUID;

/**
 * IDatabase
 */
public interface IDatabase {
    
    void loadPlayer(UUID uuid) throws Exception;
    void savePlayer(UUID uuid) throws Exception;
    void updateStats(UUID uuid) throws Exception;
    
}
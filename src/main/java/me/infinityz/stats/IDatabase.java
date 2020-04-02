package me.infinityz.stats;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.infinityz.players.SurvivalPlayer;

/**
 * IDatabase
 */
public interface IDatabase {
    
    void loadPlayer(UUID uuid) throws Exception;
    void savePlayer(UUID uuid) throws Exception;
    void updateStats(UUID uuid, SurvivalPlayer player) throws Exception;
    void updateStats(UUID uuid) throws Exception;
    void logEvent(BlockBreakEvent brk, Material type) throws Exception;
    void logEvent(BlockPlaceEvent place) throws Exception;
    
}
package me.infinityz;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.infinityz.listeners.GlobalListeners;
import me.infinityz.players.PlayerManager;
import me.infinityz.scoreboard.ScoreboardManager;

/**
 * SurvivalUniverse
 */
public class SurvivalUniverse extends JavaPlugin {
    public ScoreboardManager scoreboardManager;
    public PlayerManager playerManager;

    @Override
    public void onEnable() {
        this.scoreboardManager = new ScoreboardManager(this);
        this.playerManager = new PlayerManager(this);
        Bukkit.getPluginManager().registerEvents(new GlobalListeners(this), this);
    }

    @Override
    public void onDisable() {

    }

}
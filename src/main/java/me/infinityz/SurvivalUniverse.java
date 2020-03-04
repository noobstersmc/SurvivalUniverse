package me.infinityz;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.infinityz.listeners.GlobalListeners;
import me.infinityz.scoreboard.ScoreboardManager;

/**
 * SurvivalUniverse
 */
public class SurvivalUniverse extends JavaPlugin {
    public ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        this.scoreboardManager = new ScoreboardManager(this);
        Bukkit.getPluginManager().registerEvents(new GlobalListeners(), this);
    }

    @Override
    public void onDisable() {

    }

}
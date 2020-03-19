package me.infinityz;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.infinityz.chunks.ChunkManager;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.cities.City;
import me.infinityz.cities.CityManager;
import me.infinityz.commands.ChunkCommands;
import me.infinityz.commands.PvPCommand;
import me.infinityz.listeners.GlobalListeners;
import me.infinityz.players.PlayerManager;
import me.infinityz.scoreboard.ScoreboardManager;
import me.infinityz.utils.FileConfig;

/**
 * SurvivalUniverse
 */
public class SurvivalUniverse extends JavaPlugin {
    public ScoreboardManager scoreboardManager;
    public PlayerManager playerManager;
    public ChunkManager chunkManager;
    public CityManager cityManager;
    public FileConfig chunksFile;
    public FileConfig cityFile;

    @Override
    public void onEnable() {
        this.scoreboardManager = new ScoreboardManager(this);
        this.playerManager = new PlayerManager(this);
        this.chunkManager = new ChunkManager(this);
        this.cityManager = new CityManager(this);

        this.chunksFile = new FileConfig(this, "chunks.yml", "chunks.yml");
        this.cityFile = new FileConfig(this, "city.yml", "city.yml");
        getCommand("pvp").setExecutor(new PvPCommand(this));
        getCommand("chunk").setExecutor(new ChunkCommands(this));
        getCommand("city").setExecutor(new ChunkCommands(this));
        Bukkit.getPluginManager().registerEvents(new GlobalListeners(this), this);
        loadChunks();
        loadCities();
    }

    @Override
    public void onDisable() {

    }

    void loadChunks() {
        chunksFile.getConfigurationSection("chunks").getKeys(false).forEach(it -> {
            chunksFile.getConfigurationSection("chunks." + it).getKeys(false).forEach(cks -> {
                String keypath = "chunks." + it + "." + cks;
                UUID uuid = UUID.fromString(it);
                int x = chunksFile.getInt(keypath + ".x-coordinate");
                int z = chunksFile.getInt(keypath + ".z-coordinate");
                String world = chunksFile.getString(keypath + ".world");
                PlayerChunk chunk = new PlayerChunk(uuid, world, x, z);
                chunkManager.ownedChunksMap.put(chunk, uuid);
            });
        });
    }

    void loadCities() {
        cityFile.getConfigurationSection("Cities").getKeys(false).forEach(it -> {
            String keypath = "Cities." + it;
            int x = cityFile.getInt(keypath + ".x-center");
            int z = cityFile.getInt(keypath + ".z-center");
            int radius = cityFile.getInt(keypath + ".radius");
            String world = cityFile.getString(keypath + ".world");
            String cityName = cityFile.getString(keypath + ".city-name");

            List<UUID> helpers = new ArrayList<>();
            List<UUID> owners = new ArrayList<>();
            cityFile.getStringList(keypath + ".helpers").forEach(id -> helpers.add(UUID.fromString(id)));
            cityFile.getStringList(keypath + ".owners").forEach(id -> helpers.add(UUID.fromString(id)));

            City city = new City(cityName, world, x, z, radius);
            city.helpers = helpers.stream().toArray(UUID[]::new);
            city.owners = owners.stream().toArray(UUID[]::new);
            cityManager.cities.add(city);
        });
    }

}
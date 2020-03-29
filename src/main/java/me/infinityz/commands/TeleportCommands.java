package me.infinityz.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.infinityz.SurvivalUniverse;
import net.md_5.bungee.api.ChatColor;

/**
 * TeleportCommands
 */
public class TeleportCommands implements CommandExecutor, Listener {
    Map<UUID, Long> delay;
    Map<UUID, Boolean> no_delay;
    Map<UUID, Location> futureLocationsMap;
    SurvivalUniverse instance;

    public TeleportCommands(SurvivalUniverse instance) {
        futureLocationsMap = new HashMap<>();
        delay = new HashMap<>();
        no_delay = new HashMap<>();
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "randomtp": {
                if (!sender.hasPermission("survival.randomtp")) {
                    return true;
                }
                if (args.length < 1) {
                    return false;
                }
                final Player pl = Bukkit.getPlayer(args[0]);
                if (pl == null) {
                    sender.sendMessage("Target player is null!");
                    return true;
                }
                if (futureLocationsMap.containsKey(pl.getUniqueId())) {
                    pl.sendMessage(ChatColor.RED + "You already are teleporting!");
                    return true;
                }
                if (delay.containsKey(pl.getUniqueId()) || no_delay.containsKey(pl.getUniqueId())) {
                    pl.sendMessage(ChatColor.RED + "You already are teleporting!");
                    return true;
                }
                pl.sendMessage(ChatColor.GREEN + "Teleporting you in 5 seconds...");
                addEffectsAndSound(pl);
                // Add it to the delay map, it's a map to avoid duplicates (lazy).
                // delay.put(pl.getUniqueId(), time);
                // Start a timer task that can find the location in the mean time.
                findSafeLocation(pl, Bukkit.getWorlds().get(0));
                // Schedule a task for later (5s) to teleport.
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    final Location loc = futureLocationsMap.get(pl.getUniqueId()).clone();
                    if (loc == null) {
                        futureLocationsMap.remove(pl.getUniqueId());
                        sender.sendMessage(ChatColor.RED + "An error ocurred while teleporting you!");
                        return;
                    }
                    /*
                     * if (!delay.remove(pl.getUniqueId(), time)) return;
                     */
                    futureLocationsMap.remove(pl.getUniqueId());
                    pl.teleport(loc);
                    pl.sendMessage(ChatColor.GREEN + "Teleported!");
                    playCompletedSound(pl);

                }, 20 * 5);
                break;
            }
            case "t": {
                if (!sender.hasPermission("survival.teleport")) {
                    return true;
                }
                if (args.length < 4) {
                    return false;
                }
                final Player pl = Bukkit.getPlayer(args[0]);
                if (pl == null) {
                    sender.sendMessage("Target player is null!");
                    return true;
                }
                final Location loc = new Location(pl.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                        Integer.parseInt(args[3]));
                if (args.length > 4) {
                    loc.setWorld(Bukkit.getWorld(args[4]));
                }
                if (delay.containsKey(pl.getUniqueId()) || no_delay.containsKey(pl.getUniqueId())) {
                    pl.sendMessage(ChatColor.RED + "You already are teleporting!");
                    return true;
                }
                pl.sendMessage(ChatColor.GREEN + "Teleporting you in 5 seconds...");
                addEffectsAndSound(pl);
                // Add it to the delay map, it's a map to avoid duplicates (lazy).
                no_delay.put(pl.getUniqueId(), true);
                // Schedule a task for later (5s) to teleport.
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    no_delay.remove(pl.getUniqueId());
                    pl.teleport(loc);
                    pl.sendMessage(ChatColor.GREEN + "Teleported!");
                    playCompletedSound(pl);

                }, 20 * 5);
                break;
            }
            case "home": {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(
                            "Console can't use this command because consoles don't have a home. They are, indeed, homeless.");
                    return true;
                }
                final Player pl = (Player) sender;
                if (pl == null) {
                    sender.sendMessage("Target player is null!");
                    return true;
                }
                if (pl.getBedSpawnLocation() != null) {
                    if (delay.containsKey(pl.getUniqueId())) {
                        pl.sendMessage(ChatColor.RED + "You already are teleporting!");
                        return true;
                    }
                    final Long time = System.currentTimeMillis();
                    pl.sendMessage(ChatColor.GREEN + "Teleporting you to home in 5 seconds...");
                    addEffectsAndSound(pl);
                    // Add it to the delay map, it's a map to avoid duplicates (lazy).
                    delay.put(pl.getUniqueId(), time);
                    // Schedule a task for later (5s) to teleport.
                    Bukkit.getScheduler().runTaskLater(instance, () -> {
                        if (!delay.remove(pl.getUniqueId(), time))
                            return;
                        pl.teleport(pl.getBedSpawnLocation());
                        pl.sendMessage(ChatColor.GREEN + "Teleported!");
                        playCompletedSound(pl);

                    }, 20 * 5);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are homeless.");
                }
                return true;
            }
            case "spawn": {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(
                            "Console can't use this command because consoles don't have a home. They are, indeed, spawnless.");
                    return true;
                }
                final Player pl = (Player) sender;
                if (pl == null) {
                    sender.sendMessage("Target player is null!");
                    return true;
                }
                if (delay.containsKey(pl.getUniqueId())) {
                    pl.sendMessage(ChatColor.RED + "You already are teleporting!");
                    return true;
                }
                final Long time = System.currentTimeMillis();
                pl.sendMessage(ChatColor.GREEN + "Teleporting you to spawn in 5 seconds...");
                // Add it to the delay map, it's a map to avoid duplicates (lazy).
                delay.put(pl.getUniqueId(), time);
                addEffectsAndSound(pl);
                // Schedule a task for later (5s) to teleport.
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    if (!delay.remove(pl.getUniqueId(), time))
                        return;
                    pl.teleport(Bukkit.getWorlds().get(0).getSpawnLocation().add(0.0, 1.5, 0.0));
                    pl.sendMessage(ChatColor.GREEN + "Teleported!");
                    playCompletedSound(pl);

                }, 20 * 5);

                return true;
            }
        }

        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!delay.containsKey(e.getPlayer().getUniqueId()))
            return;
        final Player player = e.getPlayer();

        if (Math.abs(e.getTo().distance(e.getFrom())) >= 0.05) {
            delay.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.CONFUSION);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.stopSound(Sound.BLOCK_CONDUIT_AMBIENT);
            player.removePotionEffect(PotionEffectType.GLOWING);
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cTeleportation cancelled due to movement!"));
        }

    }

    void addEffectsAndSound(Player player) {
        // Give effectss
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 100));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 100));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0));
        // play sound
        player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 100000000.0F, 1.0F);
    }

    void playCompletedSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 100000000.0F, 1.0F);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    Location getRandomLocation(World world) {
        final Location loc = new Location(world, 0, 0, 0);
        loc.setX(getRandomInBetween(6000, 3000));
        loc.setZ(getRandomInBetween(6000, 3000));
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
        return loc;
    }

    boolean isValidLocation(Location loc) {
        if (loc == null)
            return false;
        final Material m = loc.getBlock().getType();
        if (m == Material.LAVA || m == Material.WATER)
            return false;
        return true;
    }

    void findSafeLocation(Player player, World world) {
        new BukkitRunnable() {
            final long initial_ms = System.currentTimeMillis();
            long ms;
            Location loc = futureLocationsMap.put(player.getUniqueId(), null);

            @Override
            public void run() {
                ms = System.currentTimeMillis();
                while (!isValidLocation(loc)) {
                    if (System.currentTimeMillis() - ms > 100)
                        return /* Let the server tick */;
                    loc = getRandomLocation(world);
                }
                loc.setX(loc.getBlockX() + 0.5);
                loc.setZ(loc.getBlockZ() + 0.5);
                loc.add(0.0, 1.5, 0.0);
                futureLocationsMap.put(player.getUniqueId(), loc);
                this.cancel();
                System.out.println("Found a location for " + player.getName() + " in " + (ms - initial_ms) / 1000.0D);
                return;
            }
        }.runTaskTimer(instance, 0, 10);
    }

    int getRandomInBetween(int max, int min) {
        return (int) (Math.random() * (max - min + 1) + min) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
    }

}

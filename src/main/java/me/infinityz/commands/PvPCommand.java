package me.infinityz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.Tristate;
import me.infinityz.players.SurvivalPlayer;
import net.md_5.bungee.api.ChatColor;

/**
 * PvPCommand
 */
public class PvPCommand implements CommandExecutor, TabCompleter, Listener {

    SurvivalUniverse instance;
    Map<UUID, Long> delay;
    String[] helpArray = { "on", "off", "true", "false", "enable", "disable" };

    public PvPCommand(SurvivalUniverse instance) {
        this.instance = instance;
        delay = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("pvp")) {
            if (sender instanceof Player) {
                SurvivalPlayer survivalPlayer = instance.playerManager.getPlayerFromId(((Player) sender).getUniqueId());
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "on":
                        case "true":
                        case "enable": {
                            if (survivalPlayer.pvp) {
                                sender.sendMessage(ChatColor.GREEN + "Pvp already enabled");
                                return true;
                            }
                            sender.sendMessage(ChatColor.GREEN + "You've enabled your PVP");
                            survivalPlayer.pvp = true;
                            break;
                        }
                        case "off":
                        case "false":
                        case "disable": {
                            if (!survivalPlayer.pvp) {
                                sender.sendMessage(ChatColor.GREEN + "Pvp already disabled");
                                return true;
                            }
                            if (delay.containsKey(survivalPlayer.playerUUID)) {
                                sender.sendMessage("Already disabling pvp...");
                                return true;
                            }
                            delay.put(survivalPlayer.playerUUID, System.currentTimeMillis());
                            addEffectsAndSound((Player) sender);
                            sender.sendMessage("Disabling your pvp in 10 seconds...");
                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                if (delay.remove(survivalPlayer.playerUUID) != null) {
                                    sender.sendMessage(
                                            ChatColor.GREEN + (survivalPlayer.pvp ? "You've disabled your PVP"
                                                    : "You've enabled your PVP"));
                                    survivalPlayer.pvp = false;

                                    playCompletedSound((Player) sender);
                                }
                            }, 20 * 10);

                            break;
                        }
                        default: {
                            sender.sendMessage(ChatColor.GREEN
                                    + (survivalPlayer.pvp ? "You've disabled your PVP" : "You've enabled your PVP"));
                            if (survivalPlayer.pvp) {
                                if (delay.containsKey(survivalPlayer.playerUUID)) {
                                    sender.sendMessage("Already disabling pvp...");
                                    return true;
                                }
                                delay.put(survivalPlayer.playerUUID, System.currentTimeMillis());
                                addEffectsAndSound((Player) sender);
                                sender.sendMessage("Disabling your pvp in 10 seconds...");
                                Bukkit.getScheduler().runTaskLater(instance, () -> {
                                    if (delay.remove(survivalPlayer.playerUUID) != null) {
                                        sender.sendMessage(
                                                ChatColor.GREEN + (survivalPlayer.pvp ? "You've disabled your PVP"
                                                        : "You've enabled your PVP"));
                                        survivalPlayer.pvp = false;
                                        playCompletedSound((Player) sender);
                                    }
                                }, 20 * 10);
                            } else {
                                sender.sendMessage(ChatColor.GREEN + (survivalPlayer.pvp ? "You've disabled your PVP"
                                        : "You've enabled your PVP"));
                                survivalPlayer.pvp = true;

                            }
                            break;
                        }
                    }
                    return true;
                }
                // Set it to the inverse when args are not present
                sender.sendMessage(ChatColor.GREEN
                        + (survivalPlayer.pvp ? "You've disabled your PVP" : "You've enabled your PVP"));
                survivalPlayer.pvp = !survivalPlayer.pvp;

            } else {
                sender.sendMessage(ChatColor.RED + "Console can't use this command.");
                return true;
            }
            // pvpdrops, globalpvp, delay

            return true;
        } else if (cmd.getName().equalsIgnoreCase("pvpdrops")) {
            if (!sender.hasPermission("op.perm"))
                return true;
            if (args.length < 1) {
                sender.sendMessage("Correct usage: /pvpdrops <true:false>");
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "true": {
                    instance.pvp_drops = true;
                    sender.sendMessage("Set to true");
                    return true;
                }
                case "false": {
                    instance.pvp_drops = false;
                    sender.sendMessage("Set to false");
                    return true;
                }
                default:
                    return true;
            }

        } else if (cmd.getName().equalsIgnoreCase("globalpvp")) {
            if (!sender.hasPermission("op.perm"))
                return true;
            if (args.length < 1) {
                sender.sendMessage("Correct usage: /globalpvp <true:false:other>");
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "true": {
                    instance.globalPvp = Tristate.TRUE;
                    sender.sendMessage("Set to true");
                    instance.config.set("global-pvp", "TRUE");
                    break;
                }
                case "false": {
                    instance.globalPvp = Tristate.FALSE;
                    sender.sendMessage("Set to false");
                    instance.config.set("global-pvp", "FALSE");
                    break;
                }
                default: {
                    instance.globalPvp = Tristate.UNKNOWN;
                    sender.sendMessage("Set to other");
                    instance.config.set("global-pvp", "UNKNOWN");
                    break;
                }
            }
            instance.config.save();
            instance.config.reload();

        }
        return false;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].isEmpty()) {
                return Arrays.asList(new String[] { "on", "off" });
            }
            final List<String> list = new ArrayList<>();
            for (String string : helpArray) {
                // Check if it matches any of the arguments available then autocomplete
                if (string.toLowerCase().startsWith(args[0].toLowerCase()))
                    list.add(string);
            }
            return list;
        }

        return null;
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPvp toggle cancelled due to movement!"));
        }

    }

}
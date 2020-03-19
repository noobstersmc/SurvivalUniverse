package me.infinityz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.players.SurvivalPlayer;
import net.md_5.bungee.api.ChatColor;

/**
 * PvPCommand
 */
public class PvPCommand implements CommandExecutor, TabCompleter {

    SurvivalUniverse instance;
    String[] helpArray = {"on", "off", "true", "false", "enable", "disable"};

    public PvPCommand(SurvivalUniverse instance) {
        this.instance = instance;
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
                            sender.sendMessage(ChatColor.GREEN + "You've enabled your PVP");
                            survivalPlayer.pvp = true;
                            break;
                        }
                        case "off":
                        case "false":
                        case "disable": {
                            sender.sendMessage(ChatColor.GREEN + "You've disabled your PVP");
                            survivalPlayer.pvp = false;
                            break;
                        }
                        default: {
                            sender.sendMessage(ChatColor.GREEN
                                    + (survivalPlayer.pvp ? "You've disabled your PVP" : "You've enabled your PVP"));
                            survivalPlayer.pvp = !survivalPlayer.pvp;
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

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 1){            
            if (args[0].isEmpty()) {
                return Arrays.asList(new String[]{"on", "off"});
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

}
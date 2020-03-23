package me.infinityz.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerChangeChunkEvent
 */
public class PlayerChangeChunkEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public Chunk from, to;
    public Location toLocation;
    public Player player;

    public PlayerChangeChunkEvent(Player player, Chunk from, Chunk to, Location toLocation) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.toLocation = toLocation;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
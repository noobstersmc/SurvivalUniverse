package me.infinityz.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerChangeChunkEvent
 */
public class PlayerChangeChunkEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    public Chunk from, to;
    public Player player;

    public PlayerChangeChunkEvent(Player player, Chunk from, Chunk to){
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    
}
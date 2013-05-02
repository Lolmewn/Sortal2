/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import nl.lolmewn.sortal.SignInfo;
import nl.lolmewn.sortal.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalPlayerTeleportEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private Player player;
    private SignInfo sign;
    private Warp end;
    
    public SortalPlayerTeleportEvent(Player player, SignInfo from, Warp endpoint) {
        this.player = player;
        this.sign = from;
        this.end = endpoint;
    }
    
    public boolean hasWarp(){
        return end != null;
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public Warp getWarp(){
        return end;
    }
    
    public SignInfo getSignInfo(){
        return sign;
    }
        
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList(){
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        cancelled = bln;
    }
}

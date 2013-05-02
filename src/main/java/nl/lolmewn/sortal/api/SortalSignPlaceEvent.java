/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalSignPlaceEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private Sign sign;
    private Player player;

    public SortalSignPlaceEvent(Sign sign, Player player) {
        this.sign = sign;
        this.player = player;
    }
    
    public Sign getSign(){
        return sign;
    }
    
    public Player getPlayer(){
        return player;
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

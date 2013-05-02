/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import nl.lolmewn.sortal.SignInfo;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalSignDestroyEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private Player player;
    private Sign sign;
    private SignInfo signInfo;

    public SortalSignDestroyEvent(Player player, Sign s, SignInfo i) {
        this.player = player;
        this.sign = s;
        this.signInfo = i;
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public Sign getSign(){
        return sign;
    }
    
    public SignInfo getSignInfo(){
        return signInfo;
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

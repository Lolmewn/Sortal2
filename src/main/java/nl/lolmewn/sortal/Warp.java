package nl.lolmewn.sortal;

import org.bukkit.Location;

/**
 *
 * @author Lolmewn
 */
public class Warp {
    
    private Location loc;

    Warp(Location loc) {
        this.loc = loc;
    }
    
    public Location getLocation(){
        return this.loc;
    }

}

package nl.lolmewn.sortal;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;

/**
 *
 * @author Lolmewn
 */
public class WarpManager {
    
    private Main plugin;
    
    private HashMap<String, Warp> warps = new HashMap<String, Warp>();
    
    private Main getPlugin(){
        return this.plugin;
    }
    
    public WarpManager(Main m){
        this.plugin = m;
        this.loadWarps();
        this.loadSigns();
    }

    private void loadWarps() {
        if(this.getPlugin().getSettings().useMySQL()){
            ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM " 
                    + this.getPlugin().getWarpTable());
            if(set == null){
                this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                this.getPlugin().getLogger().severe("Plugin is disabling!");
                this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
            }
            try {
                while(set.next()){
                    this.addWarp(set.getString("name"), new Location(
                            this.getPlugin().getServer().getWorld(set.getString("world")),
                            set.getInt("x"), set.getInt("y"), set.getInt("z"),
                            set.getFloat("yaw"), set.getFloat("pitch")));
                }
            } catch (SQLException ex) {
                this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
            }
            return;
        }
        File warpFile = new File("plugins" + File.separator + 
                "Sortal" + File.separator + "warps.txt");
        
    }

    private void loadSigns() {
        
    }
    
    public void addWarp(String name, Location loc){
        
    }
    
    public void removeWarp(String name){
        
    }
    
    public boolean hasWarp(String name){
        return this.warps.containsKey(name);
    }
    
    public Warp getWarp(String name){
        return this.warps.get(name);
    }
    
}

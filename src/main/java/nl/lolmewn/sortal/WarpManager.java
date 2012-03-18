package nl.lolmewn.sortal;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class WarpManager {
    
    private Main plugin;
    
     File warpFile = new File("plugins" + File.separator + 
                "Sortal" + File.separator + "warps.yml");
    
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
       
        YamlConfiguration c = YamlConfiguration.loadConfiguration(warpFile);
        for(String key : c.getConfigurationSection("").getKeys(false)){
            this.addWarp(key, new Location(this.getPlugin().getServer().getWorld(c.getString(key + ".world"))
                    , c.getDouble("x"), c.getDouble("y"), c.getDouble("z"), 
                    (float)c.getDouble("yaw"), (float)c.getDouble("pitch")));
        }
        this.getPlugin().getLogger().info("Warps loaded: " + this.warps.size());
    }

    private void loadSigns() {
        
    }
    
    public void addWarp(String name, Location loc){
        this.warps.put(name, new Warp(loc));
    }
    
    public void removeWarp(String name){
        
    }
    
    public boolean hasWarp(String name){
        return this.warps.containsKey(name);
    }
    
    public Warp getWarp(String name){
        return this.warps.get(name);
    }
    
    public void savaData(){
        YamlConfiguration c = YamlConfiguration.loadConfiguration(this.warpFile);
        for(String warp : this.warps.keySet()){
            Location loc = this.warps.get(warp).getLocation();
            c.set(warp + ".x", loc.getX());
            c.set(warp + ".y", loc.getY());
            c.set(warp + ".z", loc.getZ());
            c.set(warp + ".yaw", (double)loc.getYaw()); //Make double because of loading is double
            c.set(warp + ".pitch", (double)loc.getPitch());
            
        }
    }
    
}

package nl.lolmewn.sortal;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Sybren
 */
public class SignInfo {
    
    private String warp;
    private String world;
    private int x, y, z;
    private int price;
    private boolean hasPrice = false;
    
    public SignInfo(String world, int x, int y, int z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public SignInfo(String world, int x, int y, int z, String warp){
        this(world, x, y, z);
        this.warp = warp;
    }
    
    public boolean hasPrice(){
        return this.hasPrice;
    }
    
    public void setWarp(String warp){
        this.warp = warp;
    }
    
    public String getWarp(){
        return this.warp;
    }
    
    public boolean hasWarp(){
        return this.warp == null ? false : true;
    }
    
    public boolean isThisSign(String world, int x, int y, int z){
        if(this.x == x && this.y == y && this.z == z && world.equals(this.world)){
            return true;
        }
        return false;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
        this.hasPrice = true;
    }
    
    public String getLocationToString(){
        return this.world + "," + this.x + "," + this.y + "," + this.z;
    }
    
    public void save(MySQL m, String table){
        ResultSet set = m.executeQuery("SELECT * FROM " + table + 
                " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
        if(set == null){
            //dafuq? 
            System.out.println("[Sortal] ERR: ResultSet returned null");
            return;
        }
        try {
            while(set.next()){
                //a warp already is in the database, gotta update it
                if(set.getInt("x") == this.x && 
                        set.getString("world").equals(this.world) &&
                        set.getInt("y") == this.y && 
                        set.getInt("z") == this.z && 
                        set.getInt("price") == this.getPrice() && 
                        set.getString("warp").equals(this.warp)){
                    //no need to update anything
                    return;
                }
                m.executeStatement("UPDATE " + table + " SET "
                        + "warp='" + this.warp + "', "
                        + "price=" + this.getPrice()
                        + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
            }
            //It's not in the table at all
            m.executeQuery("INSERT INTO " + table + "(world, x, y, z, warp, price) VALUES ("
                    + "'" + this.world + "', "
                    + this.x + ", " + this.y + ", " + this.z
                    + ", '" + this.warp + "', " + this.getPrice() + ")");
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public void save(File f){
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set(this.getLocationToString(), this.warp + "," + this.getPrice());
        try {
            c.save(f);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void delete(MySQL m, String warpTable) {
        m.executeStatement("DELETE FROM " + warpTable + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
    }

    public void delete(File warpFile) {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(warpFile);
        c.set(this.getLocationToString(), null);
        try {
            c.save(warpFile);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
}

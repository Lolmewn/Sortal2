/*
 * UserInfo.java
 * 
 * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
 * 
 * Sortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sortal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
 */

package nl.lolmewn.sortal;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */

public class UserInfo {
    
    private String username;
    
    private HashMap<String, Integer> warpUses = new HashMap<String, Integer>();
    private HashMap<Location, Integer> locUses = new HashMap<Location, Integer>();
    
    private int redeems;

    public UserInfo(String user) {
        this.username = user;
    }
    
    public boolean hasRedeems(){
        return this.redeems != 0;
    }
    
    public boolean hasUsedWarp(String warp){
        return this.warpUses.containsKey(warp);
    }
    
    public int getUsedWarp(String warp){
        if(!this.warpUses.containsKey(warp)){
            return 0;
        }
        return this.warpUses.get(warp);
    }
    
    public void addtoUsedWarp(String warp, int add){
        if(!this.hasUsedWarp(warp)){
            this.warpUses.put(warp, add);
            return;
        }
        this.warpUses.put(warp, this.warpUses.get(warp)+add);
    }
    
    public boolean hasUsedLocation(Location loc){
        Location l = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return this.locUses.containsKey(l);
    }
    
    public int getUsedLocation(Location loc){
        if(!this.hasUsedLocation(loc)){
            return 0;
        }
        Location l = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return this.locUses.get(l);
    }
    
    public void addtoUsedLocation(Location loc, int add){
        Location l = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if(!this.hasUsedLocation(l)){
            this.locUses.put(l, add);
            return;
        }
        this.locUses.put(l, this.locUses.get(l)+add);
    }
    
    public void save(MySQL m, String table){
        for(String warp : this.warpUses.keySet()){
            ResultSet set = m.executeQuery("SELECT * FROM " + table + " WHERE warp='" + warp + "' AND player='" + username + "'");
            if(set == null){
                //thats weird..
                continue;
            }
            try {
                boolean found = false;
                while(set.next()){
                   //is already in the table
                    m.executeStatement("UPDATE " + table + " SET used=" + this.getUsedWarp(warp) + " WHERE warp='" + warp + "' AND player='" + this.username + "'");
                    found = true;
                    break;
                }
                if(!found){
                    m.executeStatement("INSERT INTO " + table + "(player, warp, used) VALUES ('" + this.username + "', '" + warp + "', " + this.getUsedWarp(warp) + ")");  
                }                
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        for(Location loc : this.locUses.keySet()){
            ResultSet set = m.executeQuery("SELECT * FROM " + table + " WHERE "
                    + "x=" + loc.getBlockX() + " AND "
                    + "y=" + loc.getBlockY() + " AND "
                    + "z=" + loc.getBlockZ() + " AND "
                    + "world='" + loc.getWorld().getName() + "'");
            if(set == null){
                //thats weird..
                continue;
            }
            try {
                boolean found = false;
                while(set.next()){
                   //is already in the table
                    m.executeStatement("UPDATE " + table + " SET used=" + this.locUses.get(loc) + " WHERE "
                    + "x=" + loc.getBlockX() + " AND "
                    + "y=" + loc.getBlockY() + " AND "
                    + "z=" + loc.getBlockZ() + " AND "
                    + "world='" + loc.getWorld().getName() + "'");
                    found = true;
                    break;
                }
                if(!found){
                    m.executeStatement("INSERT INTO " + table + "(player, x, y, z, world, used) VALUES ('" + 
                            this.username + "', " + 
                            loc.getBlockX() + ", " + 
                            loc.getBlockY() + ", " + 
                            loc.getBlockZ() + ", '" + 
                            loc.getWorld().getName() + "',"
                            + this.getUsedLocation(loc) + ")");  
                }                
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, null, ex);
            }
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
        for(String warp : this.warpUses.keySet()){
            c.set(this.username + "." + warp, this.getUsedWarp(warp));
        }
        for(Location loc : this.locUses.keySet()){
            c.set(this.username + "." + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), this.getUsedLocation(loc));
        }
        try {
            c.save(f);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }

}

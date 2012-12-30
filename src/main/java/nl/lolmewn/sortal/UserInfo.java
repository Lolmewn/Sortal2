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
    private HashMap<String, Integer> locUses = new HashMap<String, Integer>();
    
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
    
    public boolean hasUsedLocation(String loc){
        return this.locUses.containsKey(loc);
    }
    
    public int getUsedLocation(String loc){
        if(!this.hasUsedLocation(loc)){
            return 0;
        }
        return this.locUses.get(loc);
    }
    
    public void addtoUsedLocation(String loc, int add){
        if(!this.hasUsedLocation(loc)){
            this.locUses.put(loc, add);
            return;
        }
        this.locUses.put(loc, this.locUses.get(loc)+add);
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
        for(String loc : this.locUses.keySet()){
            String[] split = loc.split(",");
            ResultSet set = m.executeQuery("SELECT * FROM " + table + " WHERE "
                    + "x=" + split[1] + " AND "
                    + "y=" + split[2] + " AND "
                    + "z=" + split[3] + " AND "
                    + "world='" + split[0] + "'");
            if(set == null){
                //thats weird..
                continue;
            }
            try {
                boolean found = false;
                while(set.next()){
                   //is already in the table
                    m.executeStatement("UPDATE " + table + " SET used=" + this.locUses.get(loc) + " WHERE "
                    + "x=" + split[1] + " AND "
                    + "y=" + split[2] + " AND "
                    + "z=" + split[3] + " AND "
                    + "world='" + split[0] + "'");
                    found = true;
                    break;
                }
                if(!found){
                    m.executeStatement("INSERT INTO " + table + "(player, x, y, z, world, used) VALUES ('" + 
                            this.username + "', " + 
                            split[1] + ", " + 
                            split[2] + ", " + 
                            split[3] + ", '" + 
                            split[0] + "',"
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
        for(String loc : this.locUses.keySet()){
            c.set(this.username + "." + loc, this.getUsedLocation(loc));
        }
        try {
            c.save(f);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }

}

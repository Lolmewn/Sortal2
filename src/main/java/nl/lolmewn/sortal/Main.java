package nl.lolmewn.sortal;

import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class Main extends JavaPlugin{
    
    private WarpManager warpManager;
    private Settings settings;
    private MySQL mysql;
    
    @Override
    public void onDisable(){
        
    }
    
    @Override
    public void onEnable(){
        this.settings = new Settings(); //Also loads Localisation
        this.mysql = new MySQL(
                this.getSettings().getDbHost(), 
                this.getSettings().getDbPort(),
                this.getSettings().getDbUser(),
                this.getSettings().getDbPass(),
                this.getSettings().getDbDatabase(),
                this.getSettings().getDbPrefix());
        if(this.getMySQL().isFault()){
            this.getLogger().severe("Something is wrong with the MySQL database, switching to flatfile!");
            this.getSettings().setUseMySQL(false);
        }
        this.warpManager = new WarpManager(this);
        this.getLogger().info("Version " + this.getSettings().getVersion() + 
                " build " + this.getDescription().getVersion() + " loaded!");
    }

    public Settings getSettings() {
        return settings;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
    
    protected MySQL getMySQL(){
        return this.mysql;
    }
    
    protected String getWarpTable(){
        return this.getSettings().getDbPrefix() + "warps";
    }
}

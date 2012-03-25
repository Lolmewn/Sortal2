package nl.lolmewn.sortal;

import java.util.HashMap;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class Main extends JavaPlugin{
    
    private WarpManager warpManager;
    private Settings settings;
    private MySQL mysql;
    private Economy eco; //Vault
    
    protected HashMap<String, Integer> setcost = new HashMap<String, Integer>();
    protected HashMap<String, String> register = new HashMap<String, String>();
    
    @Override
    public void onDisable(){
        this.saveData();
        this.getServer().getScheduler().cancelTasks(this);
        this.getLogger().info("Disabled!");
    }
    
    @Override
    public void onEnable() {
        this.settings = new Settings(); //Also loads Localisation
        if (this.getSettings().useMySQL()) {
            this.mysql = new MySQL(
                    this.getSettings().getDbHost(),
                    this.getSettings().getDbPort(),
                    this.getSettings().getDbUser(),
                    this.getSettings().getDbPass(),
                    this.getSettings().getDbDatabase(),
                    this.getSettings().getDbPrefix());
            if (this.getMySQL().isFault()) {
                this.getLogger().severe("Something is wrong with the MySQL database, switching to flatfile!");
                this.getSettings().setUseMySQL(false);
            }
        }
        this.warpManager = new WarpManager(this);
        this.getCommand("sortal").setExecutor(new SortalExecutor(this));
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        if(!this.initVault()){
            this.getLogger().info("Vault error, setting costs to 0!");
            this.getSettings().setWarpCreatePrice(0);
            this.getSettings().setWarpUsePrice(0);
        }
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
            public void run() {
                new Thread(new Runnable(){public void run() {saveData();getLogger().info("Data saved!");}}).start();
                }
        }, 36000L, 36000L);
        this.getLogger().log(Level.INFO, String.format("Version %s build %s loaded!", this.getSettings().getVersion(), this.getDescription().getVersion()));
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
    
    protected String getSignTable(){
        return this.getSettings().getDbPrefix() + "signs";
    }
    
    public void saveData(){
        this.getWarpManager().saveData();
    }
    
    public boolean pay(Player p, int amount){
        if(amount == 0){
            return true;
        }
        if(initVault()){
            if(!this.eco.has(p.getName(), amount)){
                //Doesn't have enough money
                p.sendMessage(this.getSettings().getLocalisation().getNoMoney(Integer.toString(amount)));
                return false;
            }
            this.eco.withdrawPlayer(p.getName(), amount);
            p.sendMessage(this.getSettings().getLocalisation().getPaymentComplete(Integer.toString(amount)));
            return true;
        }
        //Either vault isn't found or Economy isn't found.
        return true;
    }

    private boolean initVault() {
        if(this.eco != null){
            return true;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
            //Vault not found
            return false;
        }
        this.eco = rsp.getProvider();
        if(this.eco == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
            return false;
        }
        this.getLogger().info("Hooked into Vault and Economy plugin succesfully!");
        return true;
    }
}

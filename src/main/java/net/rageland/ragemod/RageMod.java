package net.rageland.ragemod;

import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import net.rageland.ragemod.database.DatabaseHandler;
import net.rageland.ragemod.towns.TownManager;
import com.iConomy.*;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * RageMod for Bukkit
 *
 * @author BrokenTomato
 */
public class RageMod extends JavaPlugin {
    private final RageModPlayerListener playerListener;
    private final RageModBlockListener blockListener;
    private final RageModServerListener serverListener;
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private Server server; 
    private PluginManager pluginManager;
    private TownManager townManager;
    public int townCost;
    public iConomy iConomy;
    public WorldGuardPlugin worldGuard;
    public static String mainDirectory = "plugins/RageMod";
    public static PermissionHandler permissionHandler;
    public File file = new File(mainDirectory + File.separator + "config.yml");
    private String missingPermissions;
    public DatabaseHandler dbhandler = null;
    
    public RageMod() {
    	serverListener = new RageModServerListener(this);
    	townManager = new TownManager(this);
    	playerListener = new RageModPlayerListener(this);
    	blockListener = new RageModBlockListener(this);  
    	iConomy = null;
    	worldGuard = null;  
    	missingPermissions = "You don't have permissions to execute that command.";
    	
    	new File(mainDirectory).mkdir();
        if(!file.exists()){
            try {
                file.createNewFile();
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }    
        setUpConfigfile(); 
    }
    
    
    public void onEnable() {    	
    	    
        loadSettingsFromConfig();        
    	server = this.getServer();
        pluginManager = server.getPluginManager();
        
        pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, this.serverListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, this.serverListener, Event.Priority.Normal, this);
        
        setupPermissions();
        System.out.println( "RageMod is enabled!" );
    }
    
    public void onDisable() {        
        System.out.println("Goodbye world!");
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {    	       	
    	if(command.getName().equalsIgnoreCase("claimtown") && sender instanceof Player) {
    		
    		if(!this.permissionHandler.has((Player) sender, "ragemod.commands.claimtown") ){
    			sender.sendMessage(missingPermissions);
    			return true; // Nothing happens, the user don't have permissions
    		}
    		townManager.addTown(args[0], (Player) sender);	
	    	return true;
    	}       	
    	return false;
    }
    
    public Configuration load(){
        try {
            Configuration config = new Configuration(file);
            config.load();
            return config;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }
    
    public void write(String root, Object x){ //just so you know, you may want to write a boolean, integer or double to the file as well, therefore u wouldnt write it to the file as "String" you would change it to something else
    	Configuration config = load();
        config.setProperty(root, x);
        config.save();
    }

    public  String read(String root){
    	Configuration config = load();
        return config.getString(root);
    }
    
    public void setUpConfigfile() {
    	write("dbUsername", "grondal_org");
    	write("dbPassword", "6M6ggQHd");
    	write("dbAddress", "grondal.org.mysql");
    	write("dbName", "grondal_org");
    	write("Towncost", "500");
    	write("dbPort", "3306");
    }
    
    public void loadSettingsFromConfig() {
    	// Setting up the SQL connection
    	dbhandler = new DatabaseHandler(
    			read("dbAddress"), 
    			Integer.parseInt(read("dbPort")), 
    			read("dbName"),
    			read("dbUsername"), 
    			read("dbPassword"));
    	
    	// Temporary towncost
    	townCost = Integer.parseInt(read("Towncost"));
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    
    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.permissionHandler == null) {
            if (permissionsPlugin != null) {
                this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
            } else {
                
            }
        }
    }
}


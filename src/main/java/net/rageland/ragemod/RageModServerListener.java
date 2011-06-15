package net.rageland.ragemod;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;

public class RageModServerListener extends ServerListener {

	private final RageMod plugin;
	
	public RageModServerListener(RageMod instance) {
		this.plugin = instance;
	}
	
	public void onPluginDisable(PluginDisableEvent event) {
        if (plugin.iConomy != null) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                plugin.iConomy = null;
                System.out.println("RageMod un-hooked from iConomy.");
            }
        }
    } // end onPluginDisable
	
	public void onPluginEnable(PluginEnableEvent event) {
        if (plugin.iConomy == null) {
            Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                    plugin.iConomy = (iConomy)iConomy;
                    System.out.println("RageMod hooked into iConomy.");
                }
            }
        }
        
    } // end onPluginEnable
	
}

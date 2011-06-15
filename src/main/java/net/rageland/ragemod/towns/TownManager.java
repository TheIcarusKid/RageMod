package net.rageland.ragemod.towns;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.database.DatabaseHandler;

import com.iConomy.system.Account;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.RegionUtil;

public class TownManager {
	
	RageMod plugin;
	private enum TOWNERROR {UNDEFINED, NAME, POSITION};
	private TOWNERROR errorMsg = TOWNERROR.UNDEFINED;
	
	public TownManager(RageMod instance) {
		
		this.plugin = instance;
	}
	
	/**
	 * This method is run when a player performs the "/claimtown <name>" command
	 * @param name
	 * @param player
	 */	
	public void addTown(String name, Player player) {
		
		player.sendMessage("Test");
		int xPos = player.getLocation().getBlockX();
		int zPos = player.getLocation().getBlockZ();
		
		Account account = plugin.iConomy.getAccount(player.getName());
		
		if(account != null) {
			if(validTown(name, zPos, xPos)) {
				
				if(!account.getHoldings().hasOver(plugin.townCost)) {
					player.sendMessage("You did not have enough coins to establish a town!");
					return;
				}
				account.getHoldings().subtract(plugin.townCost);
				BlockVector min = new BlockVector(xPos - 40, 0, zPos - 40);				
				BlockVector max = new BlockVector(xPos + 40, 128, zPos + 40);
				
				ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
				
				plugin.worldGuard.getGlobalRegionManager().get(player.getWorld()).addRegion(region);
				String[] owners = {player.getName()};
				RegionUtil.addToDomain(region.getOwners(), owners, 0);
				
			} else {
				if(errorMsg == TOWNERROR.NAME) {
					player.sendMessage("Name of town already in use.");
				} else if(errorMsg == TOWNERROR.POSITION) {
					player.sendMessage("Position is to close to another town / spawn.");
				}
				return;
			}
		}
	}
	
	
	private boolean validTown(String name, int zPos, int xPos) {
		boolean valid = true;	
			
		ResultSet rs = plugin.dbhandler.getCurrentTowns();
		
		try {			
			while(rs.next()) {
				String dbName = rs.getString("TownName");
				int dbZPos = rs.getInt("ZCoord");
				int dbXPos = rs.getInt("XCoord");
				int dbRangeLimit = rs.getInt("RangeLimit");
				
				if(name.equalsIgnoreCase(dbName)) {
					errorMsg = TOWNERROR.NAME;
					return false;
				} 				
				if(Math.abs(zPos - dbZPos) < dbRangeLimit && Math.abs(xPos - dbXPos) < dbRangeLimit ) {
					errorMsg = TOWNERROR.POSITION;
					return false;
				}
			} // end while
		} catch (SQLException e) {
			return false;
		}		
		
		return valid;
	}

}

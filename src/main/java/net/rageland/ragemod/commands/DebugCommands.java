package net.rageland.ragemod.commands;

import java.util.ArrayList;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.Players;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DebugCommands 
{
	// /debug donation
	public static void donation(Player player) 
	{
		PlayerData playerData = Players.get(player.getName());
		int donation = RageMod.database.getRecentDonations(playerData.id_Player);
		
		player.sendMessage("The database records you with a total donation of $" + donation + " in the last month.");
	}

	public static void sanctum(Player player, String levelString) 
	{
		int level;
		World world = player.getWorld();
		PlayerData playerData = Players.get(player.getName());
		
		try
		{
			level = Integer.parseInt(levelString);
		}
		catch( Exception ex )
		{
			player.sendMessage("Invalid level.");
			return;
		}
		
		if( level < 1 || level > 5 )
		{
			player.sendMessage("Invalid level.");
			return;
		}
		
		// Pinpoint the top-left corner
		int cornerX = (int)player.getLocation().getX() - 2; // - 10;
		int cornerY = (int)player.getLocation().getY() - 1;
		int cornerZ = (int)player.getLocation().getZ() - 2; // - 10;
		Block currentBlock;
		
		ArrayList<String> layout = RageConfig.buildSanctumFloor(level);
		
	    //		c: Cobblestone
	    //		d: Dirt
	    //		p: Wood Planks
	    //		s: Stone
	    //		t: Tile (slab)
	    //		o: Obsidian
		//		O: Obsidian stack 2-high w/ glowstone on top
	    //		g: Glowstone
		//		G: Glowstone stack 2-high
	    //		w: Wool (of appropriate color)
	    //		-: Portal (inside)
	    //		|: Portal (outside)
	    //		b: Bedrock
	    //		L: Liquid (water/lava)
	    //		n: Snow
	    //		i: Iron Block
	    //		S: Special faction block (lapis/netherrack)
		for( int x = 0; x < 20; x++ )
		{
			for( int z = 0; z < 20; z++ )
			{
				currentBlock = world.getBlockAt(cornerX + x, cornerY, cornerZ + z);
				
				// Clear the air above the floor
				world.getBlockAt(cornerX + x, cornerY + 1, cornerZ + z).setType(Material.AIR);
				world.getBlockAt(cornerX + x, cornerY + 2, cornerZ + z).setType(Material.AIR);
				world.getBlockAt(cornerX + x, cornerY + 3, cornerZ + z).setType(Material.AIR);
				world.getBlockAt(cornerX + x, cornerY + 4, cornerZ + z).setType(Material.AIR);
				world.getBlockAt(cornerX + x, cornerY + 5, cornerZ + z).setType(Material.AIR);
				
				// Create bedrock below the floor, to hold liquids
				world.getBlockAt(cornerX + x, cornerY - 1, cornerZ + z).setType(Material.BEDROCK);
				world.getBlockAt(cornerX + x, cornerY - 2, cornerZ + z).setType(Material.BEDROCK);
				
				// Set the floor block(s)
				switch( layout.get(x).charAt(z) )
				{
					case 'c':
						currentBlock.setType(Material.COBBLESTONE); break;
					case 'd':
						currentBlock.setType(Material.DIRT); break;
					case 'p':
						currentBlock.setType(Material.WOOD); break;
					case 's':
						currentBlock.setType(Material.STONE); break;
					case 't':
						currentBlock.setType(Material.DOUBLE_STEP); break;
					case 'o':
						currentBlock.setType(Material.OBSIDIAN); break;
					case 'O':
						currentBlock.setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 1, cornerZ + z).setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 2, cornerZ + z).setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 3, cornerZ + z).setType(Material.OBSIDIAN);
						break;
					case 'g':
						currentBlock.setType(Material.GLOWSTONE); break;
					case 'G':
						currentBlock.setType(Material.GLOWSTONE); 
						world.getBlockAt(cornerX + x, cornerY + 1, cornerZ + z).setType(Material.GLOWSTONE);
						break;
					case 'w':
						currentBlock.setType(Material.WOOL); 
						currentBlock.setData((byte) Byte.valueOf(String.valueOf(Factions.getWoolColor(playerData.id_Faction))));
						break;
					case '-':
						currentBlock.setType(Material.OBSIDIAN); 
						world.getBlockAt(cornerX + x, cornerY + 1, cornerZ + z).setType(Material.PORTAL);
						world.getBlockAt(cornerX + x, cornerY + 2, cornerZ + z).setType(Material.PORTAL);
						world.getBlockAt(cornerX + x, cornerY + 3, cornerZ + z).setType(Material.PORTAL);
						world.getBlockAt(cornerX + x, cornerY + 4, cornerZ + z).setType(Material.OBSIDIAN);
						break;
					case '|':
						currentBlock.setType(Material.OBSIDIAN); 
						world.getBlockAt(cornerX + x, cornerY + 1, cornerZ + z).setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 2, cornerZ + z).setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 3, cornerZ + z).setType(Material.OBSIDIAN);
						world.getBlockAt(cornerX + x, cornerY + 4, cornerZ + z).setType(Material.OBSIDIAN);
						break;
					case 'b':
						currentBlock.setType(Material.BEDROCK); break;
					case 'L':
						currentBlock.setType(Material.GLASS);
						world.getBlockAt(cornerX + x, cornerY - 1, cornerZ + z).setType(Factions.getLiquidBlock(playerData.id_Faction));
						break;
					case 'n':
						currentBlock.setType(Material.SNOW_BLOCK); break;
					case 'i':
						currentBlock.setType(Material.IRON_BLOCK); break;
					case 'S':
						currentBlock.setType(Factions.getSpecialBlock(playerData.id_Faction)); break;
						
						
						
				}
			}
		}
		

		

		
	}

}

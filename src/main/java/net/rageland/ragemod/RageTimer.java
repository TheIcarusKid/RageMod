package net.rageland.ragemod;

import java.util.TimerTask;

import net.rageland.ragemod.data.Tasks;

public class RageTimer extends TimerTask
{
	private final RageMod plugin;
	int secondHand = 0;
	
	public RageTimer(final RageMod plugin) 
	{
        this.plugin = plugin;
    }
	
	// Process timer tasks
	public void run()
	{
		
		// Process per-minute tasks (separated out for efficiency)
		if( secondHand == 0 )
		{
			// Process town upkeep/taxes
			if( Tasks.getSeconds("TOWN_UPKEEP") > RageConfig.Task_TOWN_UPKEEP )
			{
				Tasks.processTownUpkeeps();
				Tasks.setComplete("TOWN_UPKEEP");
			}
			
				
			
		}
		
		if( Tasks.getSeconds("FILL_SANDLOT") > RageConfig.Task_FILL_SANDLOT )
		{
			Tasks.processFillSandlot(plugin);
			Tasks.setComplete("FILL_SANDLOT");
		}
		
		
		// Increment/reset the second hand
		secondHand++;
		if( secondHand == 60 )
			secondHand = 0;
	}	
}
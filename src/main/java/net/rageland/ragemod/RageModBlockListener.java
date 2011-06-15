package net.rageland.ragemod;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * RageMod block listener
 * @author BrokenTomato
 */
public class RageModBlockListener extends BlockListener {
    private final RageMod plugin;

    public RageModBlockListener(final RageMod plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}

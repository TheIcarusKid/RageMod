package net.rageland.ragemod;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author BrokenTomato
 */
public class RageModPlayerListener extends PlayerListener {
    private final RageMod plugin;

    public RageModPlayerListener(RageMod instance) {
        plugin = instance;
    }

    //Insert Player related code here
}


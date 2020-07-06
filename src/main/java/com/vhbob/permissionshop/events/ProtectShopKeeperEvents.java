package com.vhbob.permissionshop.events;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.vhbob.permissionshop.PShopPlugin;

public class ProtectShopKeeperEvents implements Listener {

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(PShopPlugin.getShopKeeper()))
			e.setCancelled(true);
	}

	@EventHandler
	public void chunkUnload(ChunkUnloadEvent e) {
		if (PShopPlugin.getShopKeeper() != null
				&& PShopPlugin.getShopKeeper().getLocation().getChunk().equals(e.getChunk())) {
			PShopPlugin.removeShopKeeper();
		}
	}

	@EventHandler
	public void chunkLoad(ChunkLoadEvent e) {
		File dataFile = new File(PShopPlugin.getInstance().getDataFolder() + File.separator + "data.yml");
		if (dataFile.exists()) {
			YamlConfiguration data = PShopPlugin.getPermConfig(dataFile);
			if (data.contains("NPC.loc")) {
				Location loc = (Location) data.get("NPC.loc");
				if (PShopPlugin.getShopKeeper() != null && PShopPlugin.getShopKeeper().getHealth() > 0
						&& PShopPlugin.getShopKeeper().getLocation().equals(loc))
					return;
				if (loc != null && loc.getChunk().isLoaded())
					PShopPlugin.setupShopKeeper(loc);
			}
		}
	}

}

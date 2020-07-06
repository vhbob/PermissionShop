package com.vhbob.permissionshop.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.vhbob.permissionshop.PShopPlugin;

public class RemoveShop implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("DespawnShop")) {
			if (sender.hasPermission(command.getPermission())) {
				if (PShopPlugin.getShopKeeper() == null) {
					sender.sendMessage(ChatColor.RED + "The Shopkeeper does not exist");
					return false;
				}
				// Save loc
				File dataFile = new File(PShopPlugin.getInstance().getDataFolder() + File.separator + "data.yml");
				if (!dataFile.exists()) {
					try {
						dataFile.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				YamlConfiguration data = PShopPlugin.getPermConfig(dataFile);
				data.set("NPC.loc", null);
				try {
					data.save(dataFile);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				PShopPlugin.removeShopKeeper();
			} else {
				sender.sendMessage(ChatColor.RED + "Missing Permission: " + command.getPermission());
			}
		}
		return false;
	}

}

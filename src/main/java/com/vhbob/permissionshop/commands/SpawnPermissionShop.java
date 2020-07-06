package com.vhbob.permissionshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.vhbob.permissionshop.PShopPlugin;

public class SpawnPermissionShop implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("SpawnPermissionShop")) {
			if (sender.hasPermission(command.getPermission())) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					PShopPlugin.setupShopKeeper(player.getLocation());
					player.sendMessage(ChatColor.GREEN + "Teleported the shopkeeper to your location!");
				} else {
					sender.sendMessage(ChatColor.RED + "You must be a player");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Missing permission: " + command.getPermission());
			}
		}
		return false;
	}

}

package com.vhbob.permissionshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import com.vhbob.permissionshop.PShopPlugin;

public class RemovePermission implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("RemoveShopPerm")) {
			if (sender.hasPermission(command.getPermission())) {
				if (args.length == 1) {
					Permission perm = new Permission(args[0]);
					PShopPlugin.removeSellablePerm(perm);
					sender.sendMessage(
							ChatColor.RED + perm.getName() + " was removed from the sellable permissions list");
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: /RemoveShopPerm (permission)");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Missing permission: " + command.getPermission());
			}
		}
		return false;
	}

}

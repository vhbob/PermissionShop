package com.vhbob.permissionshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import com.vhbob.permissionshop.PShopPlugin;

public class AddPermission implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("AddShopPerm")) {
			if (sender.hasPermission(command.getPermission())) {
				if (args.length >= 3) {
					Permission perm = new Permission(args[0]);
					if (GenUtil.isNumeric(args[1])) {
						double cost = Double.parseDouble(args[1]);
						String desc = "";
						for (int i = 2; i < args.length; i++)
							desc += args[i] + " ";
						PShopPlugin.addSellablePerm(perm, cost, desc);
						sender.sendMessage(
								ChatColor.GREEN + "Added the permission " + perm.getName() + " with price " + cost);
					} else {
						sender.sendMessage(ChatColor.RED + "The price must be numeric");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: /AddShopPerm (permission) (price) (description)");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Missing permission: " + command.getPermission());
			}
		}
		return false;
	}

}

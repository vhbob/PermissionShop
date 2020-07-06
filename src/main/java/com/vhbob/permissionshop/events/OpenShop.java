package com.vhbob.permissionshop.events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import com.vhbob.permissionshop.PShopPlugin;

public class OpenShop implements Listener {

	@EventHandler
	public void onClick(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked().equals(PShopPlugin.getShopKeeper())) {
			if (!e.getPlayer().hasPermission("perm.shop.use")) {
				e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to open the shop!");
				return;
			}
			Inventory inv = Bukkit.createInventory(null, 9 * (PShopPlugin.getSellablePerms().size() / 9 + 1), ChatColor
					.translateAlternateColorCodes('&', PShopPlugin.getInstance().getConfig().getString("Inv-name")));
			for (Permission perm : PShopPlugin.getSellablePerms().keySet()) {
				ItemStack pane = new ItemStack(Material.AIR);
				ArrayList<String> lore = new ArrayList<String>();
				if (e.getPlayer().hasPermission(perm)) {
					pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
					ItemMeta paneMeta = pane.getItemMeta();
					paneMeta.setDisplayName(ChatColor.GREEN + PShopPlugin.getDescriptions().get(perm));
					lore.add(ChatColor.GRAY + "You already have this permission");
					paneMeta.setLore(lore);
					pane.setItemMeta(paneMeta);
				} else {
					pane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
					ItemMeta paneMeta = pane.getItemMeta();
					paneMeta.setDisplayName(ChatColor.RED + PShopPlugin.getDescriptions().get(perm));
					lore.add(ChatColor.GRAY + "Purchase this for: $" + PShopPlugin.getSellablePerms().get(perm));
					paneMeta.setLore(lore);
					pane.setItemMeta(paneMeta);
				}
				inv.addItem(pane);
			}
			PurchasePerm.setPurchasing(e.getPlayer());
			e.getPlayer().openInventory(inv);
		}
	}

}

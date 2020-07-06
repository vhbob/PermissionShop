package com.vhbob.permissionshop.events;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import com.vhbob.permissionshop.PShopPlugin;

public class PurchasePerm implements Listener {

	private static ArrayList<Player> purchasing;
	private static HashMap<Player, Permission> confirming;
	private static HashMap<Player, Integer> confirmSlot;
	private static HashMap<Player, ItemStack> confirmSlotOldItem;

	public PurchasePerm() {
		if (confirmSlotOldItem == null)
			confirmSlotOldItem = new HashMap<Player, ItemStack>();
		if (purchasing == null)
			purchasing = new ArrayList<Player>();
		if (confirming == null)
			confirming = new HashMap<Player, Permission>();
		if (confirmSlot == null)
			confirmSlot = new HashMap<Player, Integer>();
	}

	public static void setPurchasing(Player p) {
		purchasing.add(p);
	}

	@EventHandler
	public void purchase(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getClickedInventory();
		if (inv != null && !inv.equals(player.getOpenInventory().getTopInventory()))
			return;
		if (purchasing.contains(player) && !confirming.containsKey(player)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)) {
				if (e.getCurrentItem().hasItemMeta()) {
					String desc = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
					// Check if they clicked a perm
					for (Permission perm : PShopPlugin.getDescriptions().keySet()) {
						// They did
						if (PShopPlugin.getDescriptions().get(perm).equalsIgnoreCase(desc)) {
							// Check for past confirmations
							if (confirmSlot.containsKey(player)) {
								inv.setItem(confirmSlot.get(player), confirmSlotOldItem.get(player));
							}
							// Add new confirming data
							confirmSlot.put(player, e.getSlot());
							confirmSlotOldItem.put(player, e.getCurrentItem());
							confirming.put(player, perm);
							ItemStack confirm = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
							ItemMeta confirmMeta = confirm.getItemMeta();
							confirmMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Click to Confirm");
							confirm.setItemMeta(confirmMeta);
							inv.setItem(e.getSlot(), confirm);
							break;
						}
					}
				}
			}
		} else if (confirming.containsKey(player)) {
			// Check if they have enough money
			Permission perm = confirming.get(player);
			double cost = PShopPlugin.getSellablePerms().get(perm);
			if (PShopPlugin.getEconomy().getBalance(player) >= cost) {
				PShopPlugin.getEconomy().withdrawPlayer(player, cost);
				System.out.println(PShopPlugin.getPermissions().playerAdd(player, perm.getName()));
				player.recalculatePermissions();
				player.sendMessage(ChatColor.GREEN + "Purchase successful!");
			} else {
				player.sendMessage(ChatColor.RED + "Insufficient funds!");
			}
			player.closeInventory();
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		if (purchasing.contains(player))
			purchasing.remove(player);
		if (confirming.containsKey(player))
			confirming.remove(player);
		if (confirmSlot.containsKey(player))
			confirmSlot.remove(player);
		if (confirmSlotOldItem.containsKey(player))
			confirmSlotOldItem.remove(player);
	}

}

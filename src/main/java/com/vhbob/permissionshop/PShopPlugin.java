package com.vhbob.permissionshop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.vhbob.permissionshop.commands.AddPermission;
import com.vhbob.permissionshop.commands.RemovePermission;
import com.vhbob.permissionshop.commands.RemoveShop;
import com.vhbob.permissionshop.commands.SpawnPermissionShop;
import com.vhbob.permissionshop.events.OpenShop;
import com.vhbob.permissionshop.events.ProtectShopKeeperEvents;
import com.vhbob.permissionshop.events.PurchasePerm;

import net.milkbowl.vault.economy.Economy;

public class PShopPlugin extends JavaPlugin {

	private static LivingEntity shopKeeper;
	private static PShopPlugin instance;
	private static HashMap<Permission, Double> sellablePerms;
	private static HashMap<Permission, String> descriptions;
	private static Economy econ = null;
	private static net.milkbowl.vault.permission.Permission perms = null;

	@Override
	public void onEnable() {
		// Setup vault eco
		if (!setupEconomy()) {
			Logger.getLogger("Minecraft").severe(
					String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		// Setup vault perms
		setupPermissions();
		instance = this;
		sellablePerms = new HashMap<Permission, Double>();
		descriptions = new HashMap<Permission, String>();
		saveDefaultConfig();
		// Register Commands
		getCommand("SpawnPermissionShop").setExecutor(new SpawnPermissionShop());
		getCommand("AddShopPerm").setExecutor(new AddPermission());
		getCommand("RemoveShopPerm").setExecutor(new RemovePermission());
		getCommand("DespawnShop").setExecutor(new RemoveShop());
		// Register events
		Bukkit.getPluginManager().registerEvents(new ProtectShopKeeperEvents(), this);
		Bukkit.getPluginManager().registerEvents(new OpenShop(), this);
		Bukkit.getPluginManager().registerEvents(new PurchasePerm(), this);
		// Register previous permissions and NPC
		File dataFile = new File(this.getDataFolder() + File.separator + "data.yml");
		if (dataFile.exists()) {
			YamlConfiguration data = getPermConfig(dataFile);
			if (data.contains("perms")) {
				for (String permName : data.getConfigurationSection("perms").getKeys(false)) {
					Permission perm = new Permission(permName.replace("[period]", "."));
					sellablePerms.put(perm, data.getDouble("perms." + permName + ".cost"));
					descriptions.put(perm, data.getString("perms." + permName + ".desc"));
				}
			}
			if (data.contains("NPC.loc")) {
				Location loc = (Location) data.get("NPC.loc");
				if (loc != null && loc.getChunk().isLoaded())
					setupShopKeeper(loc);
			}
		}
	}

	@Override
	public void onDisable() {
		// Save shopkeeper data
		File configFile = new File(this.getDataFolder() + File.separator + "data.yml");
		if (configFile.exists())
			configFile.delete();
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		YamlConfiguration config = getPermConfig(configFile);
		if (shopKeeper != null && shopKeeper.getLocation() != null) {
			config.set("NPC.loc", shopKeeper.getLocation());
		}
		// Save permissions

		for (Permission perm : sellablePerms.keySet()) {
			String permName = perm.getName().replace(".", "[period]");
			config.set("perms." + permName + ".cost", sellablePerms.get(perm));
			config.set("perms." + permName + ".desc", descriptions.get(perm));
		}
		try {
			config.save(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Remove shopkeeper
		removeShopKeeper();
	}

	public static LivingEntity getShopKeeper() {
		return shopKeeper;
	}

	public static void setupShopKeeper(Location loc) {
		if (shopKeeper != null)
			shopKeeper.remove();
		shopKeeper = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
		// Update data
		shopKeeper
				.setCustomName(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("NPC.name")));
		shopKeeper.setCustomNameVisible(true);
		shopKeeper.setInvulnerable(true);
		shopKeeper.setCollidable(false);
		shopKeeper.setGravity(false);
		shopKeeper.setAI(false);
		Villager v = (Villager) shopKeeper;
		v.setVillagerType(Type.DESERT);
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
		data.set("NPC.loc", PShopPlugin.getShopKeeper().getLocation());
		try {
			data.save(dataFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static PShopPlugin getInstance() {
		return instance;
	}

	public static HashMap<Permission, Double> getSellablePerms() {
		return sellablePerms;
	}

	public static void addSellablePerm(Permission perm, double cost, String description) {
		for (Permission checkPerm : sellablePerms.keySet())
			if (checkPerm.getName().equalsIgnoreCase(perm.getName())) {
				sellablePerms.remove(checkPerm);
				descriptions.remove(perm);
				break;
			}
		descriptions.put(perm, description);
		sellablePerms.put(perm, cost);
	}

	public static HashMap<Permission, String> getDescriptions() {
		return descriptions;
	}

	public static void removeSellablePerm(Permission perm) {
		for (Permission checkPerm : sellablePerms.keySet()) {
			if (checkPerm.getName().equalsIgnoreCase(perm.getName())) {
				sellablePerms.remove(checkPerm);
				break;
			}
		}
	}

	public static void removeShopKeeper() {
		if (shopKeeper != null) {
			shopKeeper.remove();
			shopKeeper = null;
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static Economy getEconomy() {
		return econ;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public static net.milkbowl.vault.permission.Permission getPermissions() {
		return perms;
	}

	public static YamlConfiguration getPermConfig(File configFile) {
		YamlConfiguration permsList = new YamlConfiguration();
		try {
			permsList.load(configFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return permsList;
	}

}

package org.l2x9.l2x9core.listeners.patches;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.l2x9.l2x9core.Main;
import org.l2x9.l2x9core.util.Utils;

public class ChinkBan implements Listener {
	Main plugin;

	public ChinkBan(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		try {
			if (plugin.getConfigBoolean("ChunkBan.Enabled")) {
				Block block = event.getBlock();
				Player player = event.getPlayer();
				int x = block.getLocation().getBlockX();
				int y = block.getLocation().getBlockY();
				int z = block.getLocation().getBlockZ();
				String worldName = block.getWorld().getName();
				if (!(player.hasPermission("chunkban.bypass"))) {
					if (isChecked(block)) {
						if (event.getBlock().getChunk().getTileEntities().length > plugin.getConfig().getInt("ChunkBan.TileEntity-Max")) {
							event.setCancelled(true);
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ChunkBan.Prevent-Message")));
						}
					}
				}
				if (block.getType() == Material.SKULL || block.getType() == Material.SKULL_ITEM) {
					if (block.getChunk().getTileEntities().length > plugin.getConfig().getInt("ChunkBan.Skull-Max")) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ChunkBan.Prevent-Message")));
					}
				}
			}
		} catch (Error | Exception throwable) {
		}
	}

	@EventHandler
	public void onSpawn(PlayerInteractEvent event) {
		try {
			if (plugin.getConfigBoolean("ChunkBan.Enabled")) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null) {
					if (event.getItem().getType() == Material.ITEM_FRAME) {
						int x = event.getPlayer().getLocation().getBlockX();
						int y = event.getPlayer().getLocation().getBlockY();
						int z = event.getPlayer().getLocation().getBlockZ();
						Player player = event.getPlayer();
						String worldName = event.getPlayer().getWorld().getName();
						int amount = 0;
						for (Entity entity : event.getPlayer().getLocation().getChunk().getEntities()) {
							if (entity instanceof ItemFrame) {
								amount++;
							}
						}
						if (amount > plugin.getConfig().getInt("ChunkBan.TileEntity-Max")) {
							event.setCancelled(true);
							event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ChunkBan.Prevent-Message")));
						}
					}
				}
			}
		} catch (Error | Exception throwable) {
		}
	}

	private boolean isChecked(Block block) {
		switch (block.getType()) {
			case FURNACE:
			case TRAPPED_CHEST:
			case ENCHANTMENT_TABLE:
			case WALL_BANNER:
			case WALL_SIGN:
			case HOPPER:
			case DROPPER:
			case DISPENSER:
			case BREWING_STAND:
			case BEACON:
			case SIGN_POST:
			case ENDER_CHEST:
			case FLOWER_POT:
			case BANNER:
				return true;
		}
		return false;
	}
}
package org.l2x9.l2x9core.listeners.patches;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.l2x9.l2x9core.util.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

	public class GateWay implements Listener {
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onEndGateway(VehicleMoveEvent event) {
			Vehicle vehicle = event.getVehicle();
			if (vehicle.getWorld().getEnvironment() == World.Environment.THE_END && vehicle.getPassengers().stream().anyMatch(e -> e instanceof Player)) {
				for (BlockFace face : BlockFace.values()) {
					Block next = vehicle.getLocation().getBlock().getRelative(face);
					if (next.getType() == Material.END_GATEWAY) {
						Player player = (Player) vehicle.getPassengers().stream().filter(e -> e instanceof Player).findAny().orElse(null);
						if (player == null) return;
						vehicle.remove();
						vehicle.eject();
						Utils.log(String.format("&1Prevented %s from crashing the server at %s)", player.getName(), Utils.formatLocation(vehicle.getLocation())));
					}
				}
			}
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		try {
			Player player = event.getPlayer();
			for (Player nearby : player.getLocation().getNearbyPlayers(50)) {
				if (!nearby.getUniqueId().toString().contains(player.getUniqueId().toString())) {
					if (nearby.getEyeLocation().getBlock().getType() == Material.PORTAL) {
						event.setCancelled(true);

					}
				}
			}
		} catch (Error | Exception throwable) {

		}
	}

	@EventHandler
	public void onEntityPortal(EntityPortalEvent event) {
		try {
			Entity entity = event.getEntity();
			if (entity.getPassenger() instanceof Player) {
				Player player = (Player) event.getEntity().getPassenger();
				if (entity instanceof Item || entity instanceof Donkey || entity instanceof Llama) {
					player.getVehicle().eject();
					event.setCancelled(true);
					entity.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void EndGatewayTeleportProtection(VehicleMoveEvent event) {
		try {
			if (event.getVehicle().getWorld().getEnvironment() == World.Environment.THE_END) {
				if (event.getVehicle().getPassenger() instanceof Player) {
					Player player = (Player) event.getVehicle().getPassenger();
					for (BlockFace face : BlockFace.values()) {
						Block next = event.getVehicle().getLocation().getBlock().getRelative(face);
						if (next.getType() == Material.END_GATEWAY) {
							int x = event.getVehicle().getLocation().getBlockX();
							int y = event.getVehicle().getLocation().getBlockY();
							int z = event.getVehicle().getLocation().getBlockZ();
							String worldString = event.getVehicle().getWorld().getName();
							event.getVehicle().eject();
							event.getVehicle().remove();
							player.chat(">>IM A FAG WHO JUST TRIED TO CRASH THE SERVER");
							Utils.kickPlayer(player, "[&b&lL2X9&r&3&lCore&r]&6 Sorry that exploit got patched ):");
							System.out.println(ChatColor.translateAlternateColorCodes('&', "&1Prevented&r&e " + player.getName() + "&r&1 at &r&e" + x + " " + y + " " + z + " &r&1in world&e " + worldString + " &r&1from crashing the server"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
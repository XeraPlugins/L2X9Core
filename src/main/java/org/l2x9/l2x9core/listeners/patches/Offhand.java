package org.l2x9.l2x9core.listeners.patches;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.l2x9.l2x9core.Main;
import org.l2x9.l2x9core.util.SecondPassEvent;
import org.l2x9.l2x9core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class Offhand implements Listener {
	HashMap<Player, Integer> offhandMap = new HashMap<>();
	Main plugin;

	public Offhand(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void PlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		try {
			Player player = event.getPlayer();
			if (isRetardTryingToCrashTheFuckingServerLikeAFuckingFaggot(player)) {
				if (offhandMap.containsKey(player)) {
					offhandMap.replace(player, offhandMap.get(player) + 1);
				} else {
					offhandMap.put(player, 1);
				}
				if (offhandMap.get(player) > 10) {
					player.kickPlayer("&cPacket Exploit Detected");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onSecond(SecondPassEvent event) {
		Utils.secondPass(offhandMap);
	}

	private boolean isRetardTryingToCrashTheFuckingServerLikeAFuckingFaggot(Player player) {
		ItemStack stack = player.getInventory().getItemInMainHand();
		ArrayList<Material> materialArrayList = new ArrayList<>();
		for (Material material : Material.values()) {
			if (material.equals(Material.BOOK)) {
				materialArrayList.add(material);
			}
			if (material.equals(Material.BOOK_AND_QUILL)) {
				materialArrayList.add(material);
			}
			if (material.toString().contains("SHULKER_BOX")) {
				materialArrayList.add(material);
			}
		}
		return materialArrayList.contains(stack.getType());
	}
}


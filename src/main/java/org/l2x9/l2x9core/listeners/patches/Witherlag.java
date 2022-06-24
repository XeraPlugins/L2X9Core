package org.l2x9.l2x9core.listeners.patches;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.l2x9.l2x9core.Main;
import org.l2x9.l2x9core.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class Witherlag implements Listener {
    Main plugin;
    private final Timer timer;
    private final HashMap<World, Integer> skulls = new HashMap<>();

    public Witherlag(Main main) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                countSkulls();
                for (World world : Bukkit.getWorlds()) {
                    List<Entity> withers = world.getEntities().stream().filter(e -> e instanceof Wither).collect(Collectors.toList());
                    if (withers.size() <= 80) continue;
                    int count = 0;
                    while (withers.size() >= 80) {
                        Wither wither = (Wither) withers.get(0);
                        count++;
                        wither.remove();
                        withers.remove(0);
                    }
                    Utils.log("&3Removed&r&a " + count + "&r&3 withers from world&r&a " + world.getName());
                }
            }
        }, 0, 10000);
    }

    @EventHandler
    public void onInteract(InventoryInteractEvent e) {
        HumanEntity player = e.getWhoClicked();

        if(player.isOp()) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK && ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta()).getPage(1).contains("\u0800") && ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta()).getPageCount() >= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.BOOK_AND_QUILL));
        }
}


    public void cancelTimer() {
        timer.cancel();
    }

    @EventHandler
    public void onSkull(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof WitherSkull) {
            WitherSkull skull = (WitherSkull) event.getEntity();
            if (skulls.get(skull.getWorld()) > 80) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        List<Entity> skulls = event.getWorld().getEntities().stream().filter(e -> e instanceof WitherSkull).collect(Collectors.toList());
        skulls.forEach(Entity::remove);
    }

    private void countSkulls() {
        for (World world : Bukkit.getWorlds()) {
            if (!skulls.containsKey(world)) {
                skulls.put(world, world.getEntities().stream().filter(e -> e instanceof WitherSkull).toArray().length);
            } else {
                skulls.replace(world, world.getEntities().stream().filter(e -> e instanceof WitherSkull).toArray().length);
            }
        }
    }
}

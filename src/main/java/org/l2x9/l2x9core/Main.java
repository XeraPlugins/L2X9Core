package org.l2x9.l2x9core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.l2x9.l2x9core.command.CommandHandler;
import org.l2x9.l2x9core.command.NotInPluginYMLException;
import org.l2x9.l2x9core.listeners.BlockPlace;
import org.l2x9.l2x9core.listeners.*;
import org.l2x9.l2x9core.listeners.antiillegal.*;
import org.l2x9.l2x9core.listeners.antilag.*;
import org.l2x9.l2x9core.listeners.patches.*;
import org.l2x9.l2x9core.util.SecondPassEvent;
import org.l2x9.l2x9core.util.TenSecondPassEvent;
import org.l2x9.l2x9core.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
	public static long startTime;
	private final PluginManager pluginManager = getServer().getPluginManager();
	private final ItemUtils itemUtils = new ItemUtils(this);
	SecondPassEvent secondPassEvent = new SecondPassEvent(getLogger(), this);
	private final HashMap<String, Integer> entityIntegerHashMap = new HashMap<>();
	ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
	ConnectionMessages connectionMessages = new ConnectionMessages(this);
	TenSecondPassEvent tenSecondPassEvent = new TenSecondPassEvent(getLogger(), this);
	public CommandHandler commandHandler;
	public final Queue<String> discordAlertQueue = new LinkedList<>();

	public void onEnable() {
		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimer(this, () -> {
			if (Utils.getTps() <= 15) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clearentities clear");
			}
		}, 20L * 1L /*<-- the initial delay */, 20L * 5L /*<-- the interval */);
		new Utils(this);
		int pluginId = 9128;
		new Metrics(this, pluginId);
		saveDefaultConfig();
		setupChunkEntityLimit();
		commandHandler = new CommandHandler(this);
		startTime = System.currentTimeMillis();
		getLogger().info("by Nate Legault enabled");
		pluginManager.registerEvents(new BlockPlace(this), this);
		pluginManager.registerEvents(new Offhand(this), this);
		pluginManager.registerEvents(new GateWay(), this);
		try {
			commandHandler.registerCommands();
		} catch (NotInPluginYMLException e) {
			e.printStackTrace();
		}
		pluginManager.registerEvents(new BookBan(), this);
		pluginManager.registerEvents(new EntityPerChunkLimit(), this);
		pluginManager.registerEvents(new ChinkBan(this), this);
		pluginManager.registerEvents(new MoveEvent(this), this);
		pluginManager.registerEvents(new CommandEvent(this), this);
		pluginManager.registerEvents(new JoinEvent(this), this);
		pluginManager.registerEvents(new Elytra(this), this);
		pluginManager.registerEvents(new EntityDamageEvent(this), this);
		pluginManager.registerEvents(new BlockRedstone(this), this);
		pluginManager.registerEvents(new WitherSpawn(), this);
		pluginManager.registerEvents(new BlockPhysics(this), this);
		pluginManager.registerEvents(new BucketEvent(this), this);
		pluginManager.registerEvents(new MinecartLag(this), this);
		//pluginManager.registerEvents(new PlayerChat(this), this);
		//pluginManager.registerEvents(new ChestLagFix(this), this);
		pluginManager.registerEvents(new dispensor(this), this);
		pluginManager.registerEvents(new PacketElytraFly(this), this);
		//pluginManager.registerEvents(connectionMessages, this);
		pluginManager.registerEvents(new DeopOnLeave(this), this);
		// AntiIllegal events
		pluginManager.registerEvents(new org.l2x9.l2x9core.listeners.antiillegal.BlockPlace(this), this);
		pluginManager.registerEvents(new HopperTansfer(this), this);
		pluginManager.registerEvents(new InventoryClose(this), this);
		pluginManager.registerEvents(new Witherlag(this), this);
		pluginManager.registerEvents(new InventoryOpen(this), this);
		pluginManager.registerEvents(new ItemPickup(this), this);
		pluginManager.registerEvents(new PlayerScroll(this), this);
		if (getConfig().getBoolean("Antiillegal.ChunkLoad-Enabled")) {
			pluginManager.registerEvents(new ChunkLoad(this), this);
		}
		//Alert system events
		// other stuff
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		//getCommand(	"toggleconnectionmessages").setExecutor(connectionMessages);
		service.scheduleAtFixedRate(() -> pluginManager.callEvent(secondPassEvent), 1, 1, TimeUnit.SECONDS);
		service.scheduleAtFixedRate(() -> pluginManager.callEvent(tenSecondPassEvent), 1, 10, TimeUnit.SECONDS);
	}

	public void onDisable() {
		getLogger().info("by Nate Legault disabled");
		if (getConfigBoolean("DeleteFortressDat")) {
			Utils.deleteFortressDat(getConfig().getString("World-name"));
		}
	}

	private String getSalDupeVersion() {
		InputStream inputStream = pluginManager.getPlugin("SalC1Dupe").getResource("plugin.yml");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		FileConfiguration pluginYml = new YamlConfiguration();
		try {
			pluginYml.load(reader);
			reader.close();
			inputStream.close();
		} catch (IOException | InvalidConfigurationException ignored) {
		}
		return pluginYml.getString("version");
	}

	public boolean getConfigBoolean(String path) {
		return getConfig().getBoolean(path);
	}

	public String getPingRole() {
		return getConfig().getString("AlertSystem.PingRole");
	}
	
	public ItemUtils getItemUtils() {
		return itemUtils;
	}

	public HashMap<String, Integer> getEntityAmounts() {
		return entityIntegerHashMap;
	}

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public void setupChunkEntityLimit() {
		try {
			List<String> entityPairs = getConfig().getStringList("EntityAmounts.TypePairs");
			List<String> validEntitys = new ArrayList<>();
			for (EntityType type : EntityType.values()) {
				validEntitys.add(type.toString());
			}
			for (String pair : entityPairs) {
				String entityType = pair.split(":")[0].toUpperCase();
				int amount = Integer.parseInt(pair.split(":")[1]);
				if (validEntitys.contains(entityType)) {
					entityIntegerHashMap.put(entityType, amount);
				} else {
					getLogger().info(ChatColor.RED + "Unknown EntityType " + entityType + " in the EntityAmounts section of the config");
				}
			}
		} catch (Error | Exception throwable) {
			getLogger().info(ChatColor.RED + "Error in the EntityAmounts section of the config missing \":\"");
		}
	}
}
package mathe172.minecraft.plugins.TeleParticles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.kitteh.vanish.staticaccess.VanishNoPacket;
import net.AptiTech.lordaragos.minecraft.PossessEm.Main;

public class Config {

	public static LinkedHashMap<String, ArrayList<TPCmdData>> commands;
	public static ArrayList<TPCmdData> toIgnoreCmds;
	public static TPLocationList fromLocationsWithTargets;
	public static TPLocationList fromLocationsWithoutTargets;
	public static TPLocationList toLocations;
	public static ArrayList<TPLocationList> allLocations;
	public static long maxDelay;	
	public static long minParticleDistance;
	public static long minSoundDistance;
	public static boolean ignoreUnknown;
	public static long minParticleDistanceSquared;
	public static long minSoundDistanceSquared;
		
	private static boolean VNPEnabled = false;
	private static boolean PEEnabled = false;
	private static Main PEInstance;
	
	private static FileConfiguration cfg = null;
	private static TeleParticles plugin = null;
	private static final long currentVersion = 3;
	private static final long defaultMaxDelay = 50;
	private static final long defaultMinParticleDistance = 5;
	private static final long defaultMinSoundDistance = 0;
	private static final boolean defaultIgnoreUnknown = true;
	private static final Logger mcLog = Logger.getLogger("Minecraft");

	
	public static void loadConfiguration(TeleParticles instance) {
		plugin = instance;
		plugin.saveDefaultConfig();
		plugin.players = new LinkedHashMap<String, TPCmdIssuerData>();
				
		commands = new LinkedHashMap<String, ArrayList<TPCmdData>>();
		toIgnoreCmds = new ArrayList<TPCmdData>();
		fromLocationsWithTargets = new TPLocationList(true, true);
		fromLocationsWithoutTargets = new TPLocationList(true, false);
		toLocations = new TPLocationList(false, true);
		allLocations = new ArrayList<TPLocationList>();
		allLocations.add(fromLocationsWithTargets);
		allLocations.add(fromLocationsWithoutTargets);
		allLocations.add(toLocations);
		
		cfg = plugin.getConfig();
		log("Loading config file");
		

		TPCmdData possessCommand = new TPCmdData(".*", ControlLevel.deny, ControlLevel.deny);
		if (PEEnabled && !isCommandSet("possess", possessCommand)) addToHashMapItem(commands, "possess", possessCommand);
		toIgnoreCmds.add(possessCommand);
		
		try {
			switch((int) cfg.get("version", 1)) {
			case 1:
				log("Config file version 1 found, converting...");
				version1Parser();
				break;
			case 2:
				log("Config file version 2 found, converting...");
				version2Parser();
				break;
			case 3:
				log("Config file version 3 found");
				version3Parser();
				break;
			}			
						
			logConfig();
		} catch (Exception ex) {
			logError("An error occured while parsing the config file:");
			logError(ex.getMessage());
		}
	}
	
	private static boolean isCommandSet(String commandName, TPCmdData commandData) {
		if (!commands.containsKey(commandName)) return false;
		
		for (TPCmdData commandType : commands.get(commandName)) {
			if (commandData.dataEquivalent(commandType)) return true;
		}
		return false;
	}
	
	public static void checkSupported() {
		Plugin vanishNoPacket = Bukkit.getPluginManager().getPlugin("VanishNoPacket");
		if (vanishNoPacket != null) {
			VNPEnabled = true;
			log("VanishNoPacket found, integrating");
		}
		
		Plugin possessEm = Bukkit.getPluginManager().getPlugin("PossessEm");
		if (possessEm != null) {
			PEEnabled = true;
			PEInstance = (Main) possessEm;
			log("PossessEm found, integrating");
		}
	}
	
	
	/*Checks if something is active that requires the effects to be disabled
	 returns the player that should be controlled (see PossessEm) or null if they should be disabled*/
	public static Player disableEffects(Player player) {
		
		if (PEEnabled) {
			try {
				if (PEInstance.possessedlist.containsValue(player)) {
					player = getFirstKeyByValue(PEInstance.possessedlist, player);
				} else if (PEInstance.possessedlist.containsKey(player)) {
					return null;
				}
			} catch (Exception e) {}
		}
		
		if (VNPEnabled) {
			try {
				if (VanishNoPacket.isVanished(player.getName())) {
					return null;	
				}
			} catch (Exception e) {}
		}	
		
		return player;
	}
	
	private static <K, V> K getFirstKeyByValue(Map<K, V> map, V value) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public static <K, I> void addToHashMapItem(LinkedHashMap<K, ArrayList<I>> hashMap, K key, I newItem) {
		ArrayList<I> oldItems = new ArrayList<I>();
		
		if (hashMap.containsKey(key)) {
			for (I oldItem : hashMap.get(key)) {
				oldItems.add(oldItem);
			}
		}
		
		oldItems.add(newItem);
		hashMap.put(key, oldItems);
	}
	
	private static void version3Parser() {

		maxDelay = cfg.getLong("maxDelay", defaultMaxDelay);
		minParticleDistance = cfg.getLong("minParticleDistance", defaultMinParticleDistance);
		minParticleDistanceSquared = minParticleDistance * minParticleDistance;
		minSoundDistance = cfg.getLong("minSoundDistance", defaultMinSoundDistance);
		minSoundDistanceSquared = minSoundDistance * minParticleDistance;
		ignoreUnknown = cfg.getBoolean("ignoreUnknown", defaultIgnoreUnknown);
		
		
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String,Object>> commandList = (ArrayList<HashMap<String, Object>>) cfg.getList("commands");
		
		if (commandList != null) {
			
			for (HashMap<String,Object> command : commandList) {

				if (command.containsKey("command") && command.get("command") instanceof String) {		
					
					String commandName = ((String) command.get("command")).toLowerCase();
					String argRegex = (command.containsKey("argRegex")) ? ((String) command.get("argRegex")).toLowerCase() : ".*";
					ControlLevel spawnParticles = fromStringToCLevel((command.containsKey("spawnParticles")) ? String.valueOf(command.get("spawnParticles")) : "0");
					ControlLevel playSound =  fromStringToCLevel((command.containsKey("playSound")) ? String.valueOf(command.get("playSound")) : "0");
					
					TPCmdData commandData = new TPCmdData(argRegex, spawnParticles, playSound);
					addToHashMapItem(commands, commandName.toLowerCase(), commandData);
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String,Object>> locationList = (ArrayList<HashMap<String, Object>>) cfg.getList("locations");
		
		if (locationList != null) {
			
			for (HashMap<String,Object> location : locationList) {
				TPLocation from = null;
				TPLocation to = null;
				if (location.containsKey("from") && location.get("from") instanceof String && String.valueOf(location.get("from")).split(",").length == 5) {
					String[] fromData = String.valueOf(location.get("from")).split(",");
					from = new TPLocation(plugin.getServer().getWorld(fromData[0]),
										  Double.valueOf(fromData[1]),
										  Double.valueOf(fromData[2]),
										  Double.valueOf(fromData[3]),
										  Double.valueOf(fromData[4]),
										  true);
				}
				if (location.containsKey("to") && location.get("to") instanceof String && String.valueOf(location.get("to")).split(",").length == 5) {
					String[] toData = String.valueOf(location.get("to")).split(",");
					to = new TPLocation(plugin.getServer().getWorld(toData[0]),
										Double.valueOf(toData[1]),
										Double.valueOf(toData[2]),
										Double.valueOf(toData[3]),
										Double.valueOf(toData[4]),
										false);
					
					if (from != null) from.neededTarget = to;
				}
				
				ControlLevel spawnParticles = fromStringToCLevel((location.containsKey("spawnParticles")) ? String.valueOf(location.get("spawnParticles")) : "0");
				ControlLevel playSound =  fromStringToCLevel((location.containsKey("playSound")) ? String.valueOf(location.get("playSound")) : "0");

				if (from != null && to != null) {
					from.spawnParticles = spawnParticles;
					from.playSound = playSound;

					addToHashMapItem(fromLocationsWithTargets, from.getWorld().getName(), from);
				} else if (from != null) {
					from.spawnParticles = spawnParticles;
					from.playSound = playSound;

					addToHashMapItem(fromLocationsWithoutTargets, from.getWorld().getName(), from);
				} else if (to != null) {
					to.spawnParticles = spawnParticles;
					to.playSound = playSound;

					addToHashMapItem(toLocations, to.getWorld().getName(), to);
				}
			}
		}
	}
	
	
	
	private static void version2Parser() {

		maxDelay = cfg.getLong("maxDelay", defaultMaxDelay);
		minParticleDistance = cfg.getLong("minParticleDistance", defaultMinParticleDistance);
		minParticleDistanceSquared = minParticleDistance * minParticleDistance;
		minSoundDistance = cfg.getLong("minSoundDistance", defaultMinSoundDistance);
		minSoundDistanceSquared = minSoundDistance * minParticleDistance;
		
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String,Object>> commandList = (ArrayList<HashMap<String, Object>>) cfg.getList("commands");
		
		if (commandList != null) {
			
			for (HashMap<String,Object> command : commandList) {

				if (command.containsKey("command") && command.get("command") instanceof String) {		
					
					String commandName = ((String) command.get("command")).toLowerCase();
					String argRegex = (command.containsKey("argRegex")) ? ((String) command.get("argRegex")).toLowerCase() : ".*";
					ControlLevel spawnParticles = (((command.containsKey("spawnParticles")) ? (boolean) command.get("spawnParticles") : true) ? ControlLevel.force : ControlLevel.none);
					ControlLevel playSound = (((command.containsKey("playSound")) ? (boolean) command.get("playSound") : true) ? ControlLevel.force : ControlLevel.none);
					
					TPCmdData commandData = new TPCmdData(argRegex, spawnParticles, playSound);
					addToHashMapItem(commands, commandName.toLowerCase(), commandData);
				}
			}
		}
		
		saveConfig();
		log("Config converted");
	}
	
	private static void version1Parser() {
				
		ConfigurationSection commandSettings = cfg.getConfigurationSection("commands");

		maxDelay = cfg.getLong("maxDelay", defaultMaxDelay);
		minParticleDistance = defaultMinParticleDistance;
		minParticleDistanceSquared = minParticleDistance * minParticleDistance;
		minSoundDistance = defaultMinSoundDistance;
		minSoundDistanceSquared = minSoundDistance * minParticleDistance;
		
		
		if (commandSettings != null) {
			Set<String> commandNames = commandSettings.getKeys(false);
			
			for (String commandName : commandNames) {
				ConfigurationSection command = commandSettings.getConfigurationSection(commandName);
				
				String argRegex = command.getString("argRegex", "");
				ControlLevel spawnParticles = (command.getBoolean("spawnParticles", true) ? ControlLevel.force : ControlLevel.none);
				ControlLevel playSound = (command.getBoolean("playSound", true) ? ControlLevel.force : ControlLevel.none);
				
				TPCmdData commandData = new TPCmdData(argRegex, spawnParticles, playSound);
				addToHashMapItem(commands, commandName.toLowerCase(), commandData);
			}
		}
				
		saveConfig();
		log("Config converted");
	}
	
	/*private static void createDefaultConfig() {
		addCommandType("spawn", new TeleParticlesCommandData("", true, true));
		
		ArrayList<HashMap<String,Object>> commandList = new ArrayList<HashMap<String, Object>>();

		HashMap<String,Object> spawnCommand = new HashMap<String,Object>();
		
		spawnCommand.put("command", "spawn");
		spawnCommand.put("argRegex", "");
		spawnCommand.put("spawnParticles", true);
		spawnCommand.put("playSound", true);
		
		commandList.add(spawnCommand);
		
		cfg.set("version", currentVersion);
		cfg.set("maxDelay",	defaultMaxDelay);
		cfg.set("commands", commandList);
		
		plugin.saveConfig();
		
		log("Default config created");
	}*/
	
	public static void saveConfig() {
		cfg.set("version", currentVersion);
		cfg.set("maxDelay", maxDelay);
		cfg.set("minParticleDistance", minParticleDistance);
		cfg.set("minSoundDistance", minSoundDistance);

		ArrayList<HashMap<String,Object>> commandList = new ArrayList<HashMap<String, Object>>();
			
		TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData> cmdIterator
			= new TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData>(commands);
		
		while (cmdIterator.hasNext()) {
			TPCmdData commandData = cmdIterator.next();
			
			if (toIgnoreCmds.contains(commandData)) continue;
			
			HashMap<String,Object> command = new HashMap<String,Object>();
			
			command.put("command", cmdIterator.previousKey());
			command.put("argRegex", commandData.argRegex);
			command.put("spawnParticles", commandData.spawnParticles.toInt());
			command.put("playSound", commandData.playSound.toInt());
			
			commandList.add(command);
		}
		
		cfg.set("commands", commandList);
		
		ArrayList<HashMap<String,Object>> locationList = new ArrayList<HashMap<String, Object>>();
		
		TPMHashMapsIterator<TPLocationList, String, TPLocation> locIterator = new TPMHashMapsIterator<TPLocationList, String, TPLocation>(allLocations);
		
		while (locIterator.hasNext()) {
			TPLocation locationData = locIterator.next();
			
			HashMap<String,Object> location = new HashMap<String,Object>();
			
			if (locIterator.previousMap().hasFrom) location.put("from", locationData.toString());
			if (locIterator.previousMap().hasTo) location.put("to", ((locationData.isFrom) ? locationData.neededTarget : locationData).toString());
			location.put("spawnParticles", locationData.spawnParticles.toInt());
			location.put("playSound", locationData.playSound.toInt());
			
			locationList.add(location);
		}
		
		cfg.set("locations", locationList);
		
		plugin.saveConfig();
	}
	
	private static void logConfig() {
		log("maxDelay set to: " + maxDelay);
		log("minParticleDistance set to: " + minParticleDistance);
		log("minSoundDistance set to: " + minSoundDistance);
		log("ignoreUnknown set to: " + ignoreUnknown);
		
		if (commands.size() > 0) {
		String[] commandInfo = new String[commands.keySet().size()];
		for (int i = 0; i < commands.keySet().toArray().length; i++) {
			String commandName = (String) commands.keySet().toArray()[i];
			commandInfo[i] = " " + commandName + ": " + commands.get(commandName).size();
		}
		log("Argument types loaded for commands:");
		log("{" + TeleParticles.join(commandInfo, ",").trim() + "}");
		}

		int from = (new TPHashMapIterator<TPLocationList, String, TPLocation>(fromLocationsWithoutTargets)).totalLength();
		int to = (new TPHashMapIterator<TPLocationList, String, TPLocation>(toLocations)).totalLength();
		int both = (new TPHashMapIterator<TPLocationList, String, TPLocation>(fromLocationsWithTargets)).totalLength();
		log("Teleport locations configured: {From: " + from + ", To: " + to + ", Both: " + both + "}");
	}
	
	private static void log(String message) {
		mcLog.log(Level.INFO, "[TeleParticles] " + message);
	}
	
	private static void logError(String message) {
		mcLog.log(Level.SEVERE, "[TeleParticles] " + message);
	}

	public static enum ControlLevel {
		none(0), force(1), deny(-1);
		
		private int intCode;
		
		private ControlLevel(int intCode) {
			this.intCode = intCode;
		}
		
		public int toInt() {
			return intCode;
		}
		
		public String toString() {
			switch(intCode) {
			case -1:
				return "deny";
			case 0:
				return "none";
			case 1:
				return "force";
			}
			return "undefined";
		}
	}

	public static ControlLevel fromIntToCLevel(int integer) {
		switch(integer) {
		case -1:
			return ControlLevel.deny;
		case 0:
			return ControlLevel.none;
		case 1:
			return ControlLevel.force;
		}
		
		return ControlLevel.none;
	}
	
	public static ControlLevel fromStringToCLevel(String string) {

		switch(string) {
		case "-1": case "deny": case "d":
			return ControlLevel.deny;
		case "0": case "none": case "n":
			return ControlLevel.none;
		case "1": case "force": case "f":
			return ControlLevel.force;
		}
		
		return ControlLevel.none;
	}
	
	private final static String clrHead = ChatColor.YELLOW.toString();
	private final static String clrErr = ChatColor.RED.toString();
	
	public static TPLocation fromStringToLoc(String string, boolean isFrom) throws NumberFormatException, IllegalArgumentException {
		String[] locData = string.split(",");
		if (locData.length == 1 && locData[0].equalsIgnoreCase("*")) {
			return null;
		} else if (locData.length == 5) {
			return new TPLocation(plugin.getServer().getWorld(locData[0]),
								  Double.valueOf(locData[1]),
								  Double.valueOf(locData[2]),
								  Double.valueOf(locData[3]),
								  Double.valueOf(locData[4]),
								  isFrom);
		} else {
			throw new IllegalArgumentException(clrHead + string + clrErr + " is not a valid location string");
		}
	}
	
	public static TPLocation fromStringToLoc(String string, boolean isFrom, Location currentLoc) throws NumberFormatException, IllegalArgumentException {
		String[] locData = string.split(",");
		if (locData.length == 2 && locData[0].equalsIgnoreCase("#")) {
			return new TPLocation(currentLoc.getWorld(), currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(), Double.valueOf(locData[1]) , isFrom);
		} else {
			return fromStringToLoc(string, isFrom);
		}
	}
}

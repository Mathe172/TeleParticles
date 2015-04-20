package mathe172.minecraft.plugins.TeleParticles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import mathe172.minecraft.plugins.TeleParticles.Config.ControlLevel;
import mathe172.minecraft.plugins.TeleParticles.TPCachedCmd.cmdId;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCmdExecutor implements CommandExecutor{

	// color values for strings
	private final String clrCmd = ChatColor.AQUA.toString();		// main commands
	private final String clrReq = ChatColor.GREEN.toString();		// required values
	private final String clrOpt = ChatColor.DARK_GREEN.toString();	// optional values
	private final String clrDesc = ChatColor.WHITE.toString();		// command descriptions
	private final String clrHead = ChatColor.YELLOW.toString();		// command listing header
	private final String clrErr = ChatColor.RED.toString();			// errors / notices
	
	private TeleParticles plugin;
	
	public TPCmdExecutor(TeleParticles instance) {
		this.plugin = instance;
	}
 	
	private LinkedHashMap<CommandSender, TPCachedCmd> cachedCommands = new LinkedHashMap<CommandSender, TPCachedCmd>();
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (sender instanceof Player) ? ((Player) sender) : null;
		
		//reload command
		if (args.length == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rld"))) {
			
			if (!hasPermission(player, "reload")) return true;

			cachedCommands = new LinkedHashMap<CommandSender, TPCachedCmd>();
			
			this.plugin.players = new LinkedHashMap<String, TPCmdIssuerData>();
			this.plugin.reloadConfig();
			Config.loadConfiguration(this.plugin);
			sender.sendMessage("TeleParticles config reloaded!");
			return true;
		
		//yes command
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("y"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			if (cachedCommands.containsKey(sender)) {
				TPCachedCmd cachedCommand = cachedCommands.get(sender);
				
				switch(cachedCommand.command) {
				case delCmd:
					TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData> dCIterator
						= new TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData>(Config.commands);
					
					while (dCIterator.hasNext()) {
						TPCmdData commandData = dCIterator.next();
						
						if (commandData.dataEquivalent((TPCmdData) cachedCommand.dataValue)) {
							dCIterator.remove();
							sender.sendMessage("Command successfully deleted");
							
							Config.saveConfig();
							return true;							
						}
					}
					
					sender.sendMessage("The command you wanted to delete doesn't exist anymore");
					return true;
					
				case modCmd:
					TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData> mCIterator
						= new TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData>(Config.commands);
					
					while (mCIterator.hasNext()) {
						TPCmdData commandData = mCIterator.next();
						
						if (commandData.dataEquivalent(((TPCmdData[]) cachedCommand.dataValue)[0])) {
							mCIterator.set(((TPCmdData[]) cachedCommand.dataValue)[1]);
							sender.sendMessage("Command successfully modified");
							
							Config.saveConfig();
							return true;							
						}
					}
					
					sender.sendMessage("The command you wanted to modify doesn't exist anymore");
					return true;
					
				case delLoc:
					TPMHashMapsIterator<TPLocationList, String, TPLocation> dLIterator
						= new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);
					
					while (dLIterator.hasNext()) {
						TPLocation locationData = dLIterator.next();
						
						if (locationData.dataEquivalent((TPLocation) cachedCommand.dataValue)) {
							dLIterator.remove();
							sender.sendMessage("Location successfully deleted");
							
							Config.saveConfig();
							return true;							
						}
					}
					
					sender.sendMessage("The location you wanted to delete doesn't exist anymore");
					return true;
				case modLoc:
					TPMHashMapsIterator<TPLocationList, String, TPLocation> mLIterator
						= new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);
					
					while (mLIterator.hasNext()) {
						TPLocation locationData = mLIterator.next();
						
						if (locationData.dataEquivalent(((TPLocation[]) cachedCommand.dataValue)[0])) {
							mLIterator.remove();
							
							TPLocation newData = ((TPLocation[]) cachedCommand.dataValue)[1];							
							if (newData.isFrom) {
								if (newData.neededTarget != null) {
									Config.addToHashMapItem(Config.fromLocationsWithTargets, newData.getWorld().getName(), newData);
								} else {
									Config.addToHashMapItem(Config.fromLocationsWithoutTargets, newData.getWorld().getName(), newData);
								}
							} else {
								Config.addToHashMapItem(Config.toLocations, newData.getWorld().getName(), newData);
							}
							
							sender.sendMessage("Location successfully modified");
							
							Config.saveConfig();
							return true;							
						}
				}
				
				sender.sendMessage("The location you wanted to modify doesn't exist anymore");
				return true;
				}
			} else {
				sender.sendMessage(clrErr + "I didn't ask you anything :)");
			}
			return true;

		//no command
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("no") || args[0].equalsIgnoreCase("n"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			if (cachedCommands.containsKey(sender)) {
				cachedCommands.remove(sender);
				sender.sendMessage("Successfully cancelled");
			} else {
				sender.sendMessage(clrErr + "I didn't ask you anything :)");
			}
			return true;
			
		//maxDelay command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("maxDelay") || args[0].equalsIgnoreCase("mD"))){
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("maxDelay is set to: " + clrCmd + Config.maxDelay);
			return true;
			
		//maxDelay command (modify)
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("maxDelay") || args[0].equalsIgnoreCase("mD"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
		
			long maxDelay = 0;
			try {
				maxDelay = Long.parseLong(args[1]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "maxDelay must be an integer bigger than 0");
				return true;
			}
			if (maxDelay < 1) {
				sender.sendMessage(clrErr + "maxDelay must be an integer bigger than 0");
				return true;
			}
			Config.maxDelay = maxDelay;
			Config.saveConfig();
			sender.sendMessage("maxDelay successfully changed to: " + clrCmd + Config.maxDelay);
			return true;
			
		//minParticleDistance command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("minParticleDistance") || args[0].equalsIgnoreCase("mPD"))){
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("maxDelay is set to: " + clrCmd + Config.minParticleDistance);
			return true;
			
		//minParticleDistance command (modify)
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("minParticleDistance") || args[0].equalsIgnoreCase("mPD"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			long minParticleDistance = 0;
			try {
				minParticleDistance = Long.parseLong(args[1]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "minParticleDistance must be a positive integer");
				return true;
			}
			if (minParticleDistance < 0) {
				sender.sendMessage(clrErr + "minParticleDistance must be a positive integer");
				return true;
			}
			Config.minParticleDistance = minParticleDistance;
			Config.saveConfig();
			sender.sendMessage("minParticleDistance successfully changed to: " + clrCmd + Config.minParticleDistance);
			return true;
			
		//minSoundDistance command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("minSoundDistance") || args[0].equalsIgnoreCase("mSD"))){
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("minSoundDistance is set to: " + clrCmd + Config.minSoundDistance);
			return true;
			
		//minSoundDistance command (modify)
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("minSoundDistance") || args[0].equalsIgnoreCase("mSD"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			long minSoundDistance = 0;
			try {
				minSoundDistance = Long.parseLong(args[1]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "minSoundDistance must be a positive integer");
				return true;
			}
			if (minSoundDistance < 1) {
				sender.sendMessage(clrErr + "minSoundDistance must be a positive integer");
				return true;
			}
			Config.minSoundDistance = minSoundDistance;
			Config.saveConfig();
			sender.sendMessage("minSoundDistance successfully changed to: " + clrCmd + Config.minSoundDistance);
			return true;
			
		//ignoreUnknown command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("ignoreUnknown") || args[0].equalsIgnoreCase("iU"))){
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("ignoreUnkown is set to: " + clrCmd + Config.ignoreUnknown);
			return true;
			
		//ignoreUnknown command (modify)
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("ignoreUnknown") || args[0].equalsIgnoreCase("iU"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			boolean ignoreUnknown;
			
			if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("true")) {
				ignoreUnknown = true;
			} else if (args[1].equalsIgnoreCase("0") || args[1].equalsIgnoreCase("no") || args[1].equalsIgnoreCase("false")) {
				ignoreUnknown = false;
			} else {
				ignoreUnknown = Boolean.parseBoolean(args[1]);
				sender.sendMessage(clrErr + "minSoundDistance must be a boolean (1,yes,true/0,no,false");
				return true;
			}
			Config.ignoreUnknown = ignoreUnknown;
			Config.saveConfig();
			sender.sendMessage("ignoreUnkown successfully changed to: " + clrCmd + Config.ignoreUnknown);
			return true;
							
		//commands command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("cmd"))){
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("The following commands are configured:");
			sender.sendMessage("(Format: <id>: {<argRegex>, <spawnParticles>, <playSound>})");			
			
			TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData> iterator 
				= new TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData>(Config.commands);
			
			String lastKey = "";
			while (iterator.hasNext()) {
				TPCmdData commandData = iterator.next();
				
				if (lastKey != iterator.previousKey()) {
					lastKey = iterator.previousKey();
					sender.sendMessage(clrHead + lastKey + ":");
				}
				
				sender.sendMessage("  [" + clrOpt + (iterator.previousIndex() + 1) + clrDesc + "]: " + commandData.toCString());
			}
			return true;
			
		//commands command (delete)
		} else if ((args.length == 2 || args.length == 3) && (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("cmd")) 
				&& (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("d"))) {
		
			if (!hasPermission(player, "readmodify")) return true;
			
			if (args.length == 2) {
				sender.sendMessage(clrErr + "You must specify an id");
				return true;
			}
			
			int id = 0;
			try {
				id = Integer.parseInt(args[2]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			if (id < 1) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			
			TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData> iterator
				= new TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData>(Config.commands);
			
			if (id > iterator.totalLength()) {
				sender.sendMessage(clrErr + "The id must not be bigger than the total amount of configured commands");
				return true;				
			}
			
			while (iterator.hasNext()) {
				TPCmdData commandData = iterator.next();
				if (id == iterator.previousIndex() + 1) {
					sender.sendMessage("Are you sure you want to delete the following command?");
					sender.sendMessage("(type " + clrCmd + "/teleParticles " + clrReq + "yes|no" + clrDesc + ")");
					sender.sendMessage(clrHead + iterator.previousKey() + clrDesc +  ": " + commandData.toCString());
					
					cachedCommands.put(sender, new TPCachedCmd(cmdId.delCmd, iterator.previousKey(), commandData));
					return true;
				}
			}
			
			sender.sendMessage(clrErr + "Sorry, something must have gone wrong");
			return true;
			
		//commands command (modify)
		} else if (args.length > 3 && args.length <= 6 && (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("cmd"))
			   && (args[1].equalsIgnoreCase("modify") || args[1].equalsIgnoreCase("mod") || args[1].equalsIgnoreCase("m"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			int id = 0;
			try {
				id = Integer.parseInt(args[2]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			if (id < 1) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			
			TPHashMapIterator<LinkedHashMap<String, ArrayList<TPCmdData>>, String, TPCmdData> iterator
				= new TPHashMapIterator<LinkedHashMap<String,ArrayList<TPCmdData>>, String, TPCmdData>(Config.commands);
		
			if (id > iterator.totalLength()) {
				sender.sendMessage(clrErr + "The id must not be bigger than the total amount of configured commands");
				return true;				
			}
			
			while (iterator.hasNext()) {
				TPCmdData commandData = iterator.next();
				if (id == iterator.previousIndex() + 1) {
					
					TPCmdData newData;
					try {
						newData = parseUserInput(commandData, args, 3);
					} catch (IllegalArgumentException e) {
						sender.sendMessage((e.getMessage() != null) ? e.getMessage() : clrErr + clrErr + "Sorry, something must have gone wrong");
						return true;
					}
					
					sender.sendMessage("Are you sure you want to modify the following command?");
					sender.sendMessage("(type " + clrCmd + "/teleParticles " + clrReq + "yes|no" + clrDesc + ")");
					sender.sendMessage(clrHead + iterator.previousKey() + clrDesc +  ": " + commandData.toCString());
					sender.sendMessage("to: " + newData.toCString());
					
					cachedCommands.put(sender, new TPCachedCmd(cmdId.modCmd, iterator.previousKey(), new TPCmdData[]{commandData, newData}));
					return true;
				}
			}
			
			sender.sendMessage(clrErr + "Sorry, something must have gone wrong");
			return true;
			
		//commands command (add)
		} else if (args.length > 3 && args.length <= 6 && (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("cmd"))
			   && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("a"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			String commandName = args[2];
			
			TPCmdData newData;
			try {
				newData = parseUserInput(new TPCmdData(".*", ControlLevel.none, ControlLevel.none), args, 3);
			} catch (IllegalArgumentException e) {
				sender.sendMessage((e.getMessage() != null) ? e.getMessage() : clrErr + clrErr + "Sorry, something must have gone wrong");
				return true;
			}
			
			Config.addToHashMapItem(Config.commands, commandName, newData);
			
			Config.saveConfig();
			sender.sendMessage("Command successfully added:");
			sender.sendMessage(clrHead + commandName + clrDesc +  ": " + newData.toCString());
			return true;
				
		//locations command (read)
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("locations") || args[0].equalsIgnoreCase("loc"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			sender.sendMessage("The following locations are configured:");
			sender.sendMessage("(Format: <id>: {<from>, <to>, <spawnParticles>, <playSound>})");	
			
			TPMHashMapsIterator<TPLocationList, String, TPLocation> iterator = new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);
			
			while (iterator.hasNext()) {
				TPLocation locationData = iterator.next();
				
				sender.sendMessage("  [" + clrOpt + (iterator.previousIndex() + 1) + clrDesc + "]: " + locationData.toCString());
			}
			return true;
		
		//locations command (delete)
		} else if ((args.length == 2 || args.length == 3) && (args[0].equalsIgnoreCase("locations") || args[0].equalsIgnoreCase("loc"))
				&& (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("d"))) {
		
			if (!hasPermission(player, "readmodify")) return true;
			
			if (args.length == 2) {
				sender.sendMessage(clrErr + "You must specify an id");
				return true;
			}
			
			int id = 0;
			try {
				id = Integer.parseInt(args[2]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			if (id < 1) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			
			TPMHashMapsIterator<TPLocationList, String, TPLocation> iterator
				= new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);
			
			if (id > iterator.totalLength()) {
				sender.sendMessage(clrErr + "The id must not be bigger than the total amount of configured locations");
				return true;				
			}
			
			while (iterator.hasNext()) {
				TPLocation locationData = iterator.next();
				if (id == iterator.previousIndex() + 1) {
					sender.sendMessage("Are you sure you want to delete the following location?");
					sender.sendMessage("(type " + clrCmd + "/teleParticles " + clrReq + "yes|no" + clrDesc + ")");
					sender.sendMessage(locationData.toCString());
					
					cachedCommands.put(sender, new TPCachedCmd(cmdId.delLoc, iterator.previousKey(), locationData));
					return true;
				}
			}	
			
			sender.sendMessage(clrErr + "Sorry, something must have gone wrong");
			return true;
			
		//locations command (modify)
		} else if (args.length > 3 && args.length <= 7 && (args[0].equalsIgnoreCase("locations") || args[0].equalsIgnoreCase("loc"))
			   && (args[1].equalsIgnoreCase("modify") || args[1].equalsIgnoreCase("mod") || args[1].equalsIgnoreCase("m"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
			
			int id = 0;
			try {
				id = Integer.parseInt(args[2]);
			} catch(NumberFormatException ex) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			if (id < 1) {
				sender.sendMessage(clrErr + "The id must be a positive integer");
				return true;
			}
			
			TPMHashMapsIterator<TPLocationList, String, TPLocation> iterator
				= new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);
			
			if (id > iterator.totalLength()) {
				sender.sendMessage(clrErr + "The id must not be bigger than the total amount of configured locations");
				return true;				
			}
			
			while (iterator.hasNext()) {
				TPLocation locationData = iterator.next();
				if (id == iterator.previousIndex() + 1) {
					
					TPLocation newData;
					try {
						newData = parseUserInput(locationData, args, 3, player);
					} catch (IllegalArgumentException e) {
						sender.sendMessage((e.getMessage() != null) ? e.getMessage() : clrErr + clrErr + "Sorry, something must have gone wrong");
						return true;
					}
					
					sender.sendMessage("Are you sure you want to modify the following location?");
					sender.sendMessage("(type " + clrCmd + "/teleParticles " + clrReq + "yes|no" + clrDesc + ")");
					sender.sendMessage(locationData.toCString());
					sender.sendMessage("to: " + newData.toCString());
					
					cachedCommands.put(sender, new TPCachedCmd(cmdId.modLoc, iterator.previousKey(), new TPLocation[]{locationData, newData}));
					return true;
				}
			}
			
			sender.sendMessage(clrErr + "Sorry, something must have gone wrong");
			return true;
			
		//locations command (add)
		} else if (args.length > 2 && args.length <= 6 && (args[0].equalsIgnoreCase("locations") || args[0].equalsIgnoreCase("loc"))
			   && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("a"))) {
			
			if (!hasPermission(player, "readmodify")) return true;
						
			TPLocation newData;
			try {
				newData = parseUserInput(null, args, 2, player);
			} catch (IllegalArgumentException e) {
				sender.sendMessage((e.getMessage() != null) ? e.getMessage() : clrErr + clrErr + "Sorry, something must have gone wrong");
				return true;
			}
			
			if (newData.isFrom) {
				if (newData.neededTarget != null) {
					Config.addToHashMapItem(Config.fromLocationsWithTargets, newData.getWorld().getName(), newData);
				} else {
					Config.addToHashMapItem(Config.fromLocationsWithoutTargets, newData.getWorld().getName(), newData);
				}
			} else {
				Config.addToHashMapItem(Config.toLocations, newData.getWorld().getName(), newData);
			}
			
			Config.saveConfig();
			sender.sendMessage("Command successfully added:");
			sender.sendMessage(newData.toCString());
			return true;
			
		//matchingLocations command
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("matchingLocations") || args[0].equalsIgnoreCase("mLocations")
				   || args[0].equalsIgnoreCase("matching") || args[0].equalsIgnoreCase("mL")
				   || args[0].equalsIgnoreCase("nearbyLocations") || args[0].equalsIgnoreCase("nLocations")
				   || args[0].equalsIgnoreCase("nearby") || args[0].equalsIgnoreCase("nL"))) {
			
			if (player == null) {
				sender.sendMessage(clrErr + "This command is only available for players");
				return true;
			}
			
			if (!hasPermission(player, "readmodify")) return true;
				
			sender.sendMessage("Your current location matches the following configured locations");
			
			TPMHashMapsIterator<TPLocationList, String, TPLocation> iterator
				= new TPMHashMapsIterator<TPLocationList, String, TPLocation>(Config.allLocations);	
			
			while (iterator.hasNext()) {
				TPLocation location = iterator.next();
				
				if (iterator.previousMap().hasFrom) {
					if (location.isLocAccepted(player.getLocation())) {
						sender.sendMessage(location.toCStringFromMarked());
					}
				}
				if (iterator.previousMap().hasTo) {
					if ((location.neededTarget == null ? location : location.neededTarget).isLocAccepted(player.getLocation())) {
						sender.sendMessage(location.toCStringToMarked());
					}
				}
			}
			
			return true;
			
		//something unknown
		} else {
			if (!hasPermission(player, "readmodify")) return true;

			int page = (player == null) ? 0 : 1;
			if (args.length == 1)
			{
				try
				{
					page = Integer.parseInt(args[0]);
				}
				catch(NumberFormatException ex)
				{
				}
				if (page > 3)
					page = 1;
			}

			sender.sendMessage(clrHead + "Teleparticles - commands " + (page > 0 ? " " + page + "/3" : "") + ":");

			String cmd = clrCmd + ((player == null) ? "teleP" : "/teleP");
			
			if (page == 0 || page == 1)	{

				sender.sendMessage(cmd + " reload/rld" + clrDesc + " reloads the configuration file from disk");
				sender.sendMessage(cmd + " maxDelay/mD" + clrOpt + " [<value>]" + clrDesc + " gets/sets the value");
				sender.sendMessage(cmd + " minParticleDistance/mPD" + clrOpt + " [<value>]" + clrDesc + " gets/sets the value");
				sender.sendMessage(cmd + " minSoundDistance/mSD" + clrOpt + " [<value>]" + clrDesc + " gets/sets the value");
				sender.sendMessage(cmd + " ignoreUnknown/iU" + clrOpt + " [<value>]" + clrDesc + " gets/sets the value");				
				sender.sendMessage(cmd + " commands/cmd" + clrDesc + " outputs a list of all configured commands");
				sender.sendMessage(cmd + " commands/cmd delete/del/d" + clrReq + " <id>" + clrDesc + " deletes the command");
				sender.sendMessage(cmd + " commands/cmd modify/mod/m" + clrReq + " <id> <data>" + clrDesc + " modifies it");
				sender.sendMessage(cmd + " commands/cmd add/a" + clrReq + " <command> <data>" + clrDesc + " adds a new one");
				if (page == 1)
					sender.sendMessage(cmd + " 2" + clrDesc + " - view second page of commands.");
			}
			if (page == 0 || page == 2)	{
				sender.sendMessage("There are two possible format for " + clrReq + "<data>" + clrDesc + ":");
				sender.sendMessage(clrReq + "<argRegex>" + clrOpt + " [<spawnParticles>] [<playSound>]" + clrDesc + " or " + clrReq + "<id>:<value>");
				sender.sendMessage("(Possible values for " + clrReq + "<id>" + clrDesc + " are: " + clrReq + "arg/a" + clrDesc + ", " + clrReq + "particles/p" + clrDesc + ", " + clrReq + "sound/s" + clrDesc + ")");
				sender.sendMessage("Values " + clrReq + "particles" + clrDesc + " & " + clrReq + "sound" + clrDesc + ": " + clrReq + "-1/deny/d" + clrDesc + ", " + clrReq + "0/none/n" + clrDesc + ", " + clrReq + "1/force/f");
				sender.sendMessage(cmd + " locations/loc" + clrDesc + "outputs a list of all configured locations");
				sender.sendMessage(cmd + " locations/loc delete/del/d " + clrReq + "<id>" + clrDesc + " deletes the location");
				sender.sendMessage(cmd + " locations/loc modify/mod/m " + clrReq + "<id> <data>" + clrDesc + " modifies it");
				sender.sendMessage(cmd + " locations/loc add|a " + clrReq + "<data>" + clrDesc + " adds a new location one");
				if (page == 2)
					sender.sendMessage(cmd+" 3" + clrDesc + " - view third page of commands.");
			}
			if (page == 0 || page == 3)	{
				sender.sendMessage("The only difference concerning the format of " + clrReq + "<newData>" + clrDesc + " is:");
				sender.sendMessage(clrReq + "from|f" + clrDesc + ", " + clrReq + "to/t" + clrDesc + " instead of " + clrReq + "arg/a");
				sender.sendMessage("Valid location formats: ");
				sender.sendMessage(clrReq + "<world>,<x>,<y>,<z>,<tolerance>" + clrDesc + " and " + clrReq + "*" + clrDesc + " (to allow any location)");
				sender.sendMessage("If using the command as player, " + clrReq + "#,<tolerance>" + clrDesc + " (current location of the player) is also possible");
				sender.sendMessage(cmd + " matchingLocations/mLocations/matching/mL" +  clrDesc + " and");
				sender.sendMessage(cmd + " nearbyLocations/nLocations/nearby/nL" + clrDesc + "output all locations matching the current position");
				if (page == 3)
					sender.sendMessage(cmd + clrDesc + " - view first page of commands.");
			}
		}
		return true;
	}

	private TPLocation parseUserInput(TPLocation defaultData, String[] args, int startIndex, Player player) throws IllegalArgumentException {
		String[] parsedArgs = new String[args.length - startIndex];
		for (int i = startIndex; i < args.length; i++) {
			parsedArgs[i - startIndex] = args[i];
		}
		parsedArgs = TeleParticles.join(parsedArgs, " ").split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for (int i = 0; i < parsedArgs.length; i++) {
			parsedArgs[i] = parsedArgs[i].replace("\"", "");
		}
		
		List<String> identifiers = Arrays.asList("from", "f", "to", "t", "spawnparticles", "particles", "p", "playsound", "sound", "s");
				
		boolean hasIdentifiers = true;
		
		for (int i = 0; i < parsedArgs.length; i++) {
			hasIdentifiers = hasIdentifiers && identifiers.contains(parsedArgs[i].split(":")[0].toLowerCase());
		}

		boolean fromSet = false;
		boolean toSet = false;
		
		TPLocation from = null;
		TPLocation to = null;
		ControlLevel spawnParticles = null;
		ControlLevel playSound = null;
		
		if (hasIdentifiers) {
			for (int i = 0; i < parsedArgs.length; i++) {
				switch(parsedArgs[i].split(":")[0].toLowerCase()) {
				case "from": case "f":
					if (!fromSet) {
						if (player != null) {
							from = Config.fromStringToLoc(parsedArgs[i].split(":")[1], true, player.getLocation());
						} else if (!parsedArgs[i].split(":")[1].contains("#")) {
							from = Config.fromStringToLoc(parsedArgs[i].split(":")[1], true);
						} else {
							throw new IllegalArgumentException(clrErr + "You cannot use " + clrHead + "#" + clrErr + " from console");
						}
						fromSet = true;
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "from" + clrErr + " twice");
					}
					break;
				case "to": case "t":
					if (!toSet) {
						if (player != null) {
							to = Config.fromStringToLoc(parsedArgs[i].split(":")[1], false, player.getLocation());
						} else if (!parsedArgs[i].split(":")[1].contains("#")) {
							to = Config.fromStringToLoc(parsedArgs[i].split(":")[1], false);
						} else {
							throw new IllegalArgumentException(clrErr + "You cannot use " + clrHead + "#" + clrErr + " from console");
						}
						toSet = true;
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "to" + clrErr + " twice");
					}
					break;
				case "spawnparticles": case "particles": case "p":
					if (spawnParticles == null) {
						spawnParticles = Config.fromStringToCLevel(parsedArgs[i].split(":")[1]);
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "spawnParticles" + clrErr + " twice");
					}
					break;
				case "playsound": case "sound": case "s":
					if (playSound == null) {
						playSound = Config.fromStringToCLevel(parsedArgs[i].split(":")[1]);
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "playSound" + clrErr + " twice");
					}
					break;
				}
			}
		} else {
			if (player != null) {
				from = Config.fromStringToLoc(parsedArgs[0].split(":")[1], true, player.getLocation());
			} else if (!parsedArgs[0].split(":")[1].contains("#")) {
				from = Config.fromStringToLoc(parsedArgs[0].split(":")[1], true);
			} else {
				throw new IllegalArgumentException(clrErr + "You cannot use " + clrHead + "#" + clrErr + " from console");
			}	
			
			if (parsedArgs.length > 1) {
				if (player != null) {
					to = Config.fromStringToLoc(parsedArgs[1].split(":")[1], false, player.getLocation());
				} else if (!parsedArgs[1].split(":")[1].contains("#")) {
					to = Config.fromStringToLoc(parsedArgs[1].split(":")[1], false);
				} else {
					throw new IllegalArgumentException(clrErr + "You cannot use " + clrHead + "#" + clrErr + " from console");
				}
			}
			if (parsedArgs.length > 2) {
				spawnParticles = Config.fromStringToCLevel(parsedArgs[2]);
			}
			if (parsedArgs.length > 3) {
				playSound = Config.fromStringToCLevel(parsedArgs[3]);
			}
		}
		
		if (defaultData != null) {
			if (!fromSet && defaultData.isFrom) {
				from = defaultData;
				fromSet = true;
			}
			if (!toSet && defaultData.isFrom && defaultData.neededTarget != null) {
				to = defaultData.neededTarget;
				toSet = true;
			}
			if (!toSet && !defaultData.isFrom) {
				to = defaultData;
				toSet = true;
			}
		}
		
		if (from == null && to == null) {
			throw new IllegalArgumentException(clrErr + "You must either define " + clrHead + "from" + clrErr + " or " + clrHead + "to");
		}
		if (spawnParticles == null) {
			spawnParticles = (defaultData != null) ? defaultData.spawnParticles: ControlLevel.none;
		}
		if (playSound == null) {
			playSound = (defaultData != null) ? defaultData.playSound: ControlLevel.none;
		}
		
		TPLocation result;
		if (from != null && to != null) {
			result = from;
			result.neededTarget = to;
		} else if (from != null) {
			result = from;
		} else {
			result = to;
		}
		
		result.spawnParticles = spawnParticles;
		result.playSound = playSound;
		
		return result;
	}

	private TPCmdData parseUserInput(TPCmdData defaultData, String[] args, int startIndex) throws IllegalArgumentException {
		String[] parsedArgs = new String[args.length - startIndex];
		for (int i = startIndex; i < args.length; i++) {
			parsedArgs[i - startIndex] = args[i];
		}
		parsedArgs = TeleParticles.join(parsedArgs, " ").split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for (int i = 0; i < parsedArgs.length; i++) {
			parsedArgs[i] = parsedArgs[i].replace("\"", "");
		}
		
		List<String> identifiers = Arrays.asList("argregex", "arg", "a", "spawnparticles", "particles", "p", "playsound", "sound", "s");
		
		boolean hasIdentifiers = true;
		
		for (int i = 0; i < parsedArgs.length; i++) {
			hasIdentifiers = hasIdentifiers && identifiers.contains(parsedArgs[i].split(":")[0].toLowerCase());
		}
		
		String argRegex = null;
		ControlLevel spawnParticles = null;
		ControlLevel playSound = null;
		if (hasIdentifiers) {
			for (int i = 0; i < parsedArgs.length; i++) {
				switch(parsedArgs[i].split(":")[0].toLowerCase()) {
				case "argregex": case "arg": case "a":
					if (argRegex == null) {
						argRegex = parsedArgs[i].split(":")[1];
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "argRegex" + clrErr + " twice");
					}
					break;
				case "spawnparticles": case "particles": case "p":
					if (spawnParticles == null) {
						spawnParticles = Config.fromStringToCLevel(parsedArgs[i].split(":")[1]);
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "spawnParticles" + clrErr + " twice");
					}
					break;
				case "playsound": case "sound": case "s":
					if (playSound == null) {
						playSound = Config.fromStringToCLevel(parsedArgs[i].split(":")[1]);
					} else {
						throw new IllegalArgumentException(clrErr + "You cannot define " + clrHead + "playSound" + clrErr + " twice");
					}
					break;
				}
			}
		} else {
			argRegex = parsedArgs[0];
			if (parsedArgs.length > 1) {
				spawnParticles = Config.fromStringToCLevel(parsedArgs[1]);
			}
			if (parsedArgs.length > 2) {
				playSound = Config.fromStringToCLevel(parsedArgs[2]);
			}
		}
		
		if (argRegex == null) {
			argRegex = defaultData.argRegex;
		}
		if (spawnParticles == null) {
			spawnParticles = defaultData.spawnParticles;
		}
		if (playSound == null) {
			playSound = defaultData.playSound;
		}
		
		return new TPCmdData(argRegex, spawnParticles, playSound);
	}
	
	private boolean hasPermission(Player player, String permission) {
		if (player == null) return true;
		
		if (player.hasPermission("teleparticles." + permission)) {
			return true;
		} else {
			player.sendMessage(clrErr + "You don't have permission to do that");
			return false;
		}
	}
}

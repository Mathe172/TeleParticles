package mathe172.minecraft.plugins.TeleParticles;

import java.util.LinkedHashMap;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleParticles extends JavaPlugin implements Plugin {
	public LinkedHashMap<String, TPCmdIssuerData> players;
	
	public void onEnable(){
				
		Config.checkSupported();
		
		Config.loadConfiguration(this);
		
		getServer().getPluginManager().registerEvents(new TPListener(this), this);
		getServer().getPluginManager().registerEvents(new TPCommandListener(this), this);
		getCommand("TeleParticles").setExecutor(new TPCmdExecutor(this));
	}
	
	public static String join(String[] r, String d)
	{
	        StringBuilder sb = new StringBuilder();
	        int i;
	        for(i=0;i<r.length-1;i++)
	            sb.append(r[i]+d);
	        return sb.toString()+r[i];
	}
	
	public static String[] processCommand(String command) {
		String commandName = command.split(" ")[0];
		String[] commandArgs = {};
		if (command.contains(" ") && command.split(" ").length > 1) {
			commandArgs = command.substring(command.indexOf(" ") + 1).split(" ");
		}
		
		String[] processedCommand = {commandName, (commandArgs.length > 0) ? join(commandArgs, " ") : ""};
		return processedCommand;
	}
}

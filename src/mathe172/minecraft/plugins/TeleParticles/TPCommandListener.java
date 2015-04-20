package mathe172.minecraft.plugins.TeleParticles;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class TPCommandListener implements Listener {

	TeleParticles plugin;
	
	public TPCommandListener(TeleParticles instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer() == null || event.getMessage() == null)
			return;
		
		Player player = event.getPlayer();
		String message = event.getMessage();
		if (message.startsWith("/")) 
			message = message.substring(1);
						
		if (!Config.commands.containsKey(message.split(" ")[0].toLowerCase()))
			return;
				
		String[] pMessage = TeleParticles.processCommand(message);
		ArrayList<TPCmdData> commandTypes = Config.commands.get(pMessage[0].toLowerCase());
		for (TPCmdData commandData : commandTypes) {
			if (pMessage[1].toLowerCase().matches(commandData.argRegex)) {
				plugin.players.put(player.getName(), new TPCmdIssuerData(System.currentTimeMillis(), commandData.spawnParticles, commandData.playSound));
			}
		}
	}
}

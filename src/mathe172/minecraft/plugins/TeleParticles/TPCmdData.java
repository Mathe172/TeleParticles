package mathe172.minecraft.plugins.TeleParticles;

import org.bukkit.ChatColor;

import mathe172.minecraft.plugins.TeleParticles.Config.ControlLevel;

public class TPCmdData {
	public String argRegex;
	public ControlLevel spawnParticles;
	public ControlLevel playSound;
		
	public TPCmdData(String argRegex, ControlLevel spawnParticles, ControlLevel playSound) {
		this.argRegex = argRegex;
		this.spawnParticles = spawnParticles;
		this.playSound = playSound;
	}
	
	public boolean dataEquivalent(TPCmdData command) {
		return this.argRegex == command.argRegex && this.spawnParticles == command.spawnParticles && this.playSound == command.playSound;
	}
	
	private final String clrCmd = ChatColor.AQUA.toString();
	private final String clrDesc = ChatColor.WHITE.toString();
	
	public String toCString() {
		return clrDesc + "{" + clrCmd + this.argRegex + clrDesc + ", " + clrCmd + this.spawnParticles.toString() + clrDesc + ", " + clrCmd + this.playSound.toString() + clrDesc + "}";
	}
}

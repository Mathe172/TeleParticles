package mathe172.minecraft.plugins.TeleParticles;

import mathe172.minecraft.plugins.TeleParticles.Config.ControlLevel;

public class TPCmdIssuerData {
	public long issueTimestamp;
	public ControlLevel spawnParticles;
	public ControlLevel playSound;
	
	public TPCmdIssuerData(long issueTimestamp, ControlLevel spawnParticles, ControlLevel playSound) {
		this.issueTimestamp = issueTimestamp;
		this.spawnParticles = spawnParticles;
		this.playSound = playSound;
	}
}

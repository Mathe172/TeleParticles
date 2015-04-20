package mathe172.minecraft.plugins.TeleParticles;

import mathe172.minecraft.plugins.TeleParticles.Config.ControlLevel;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class TPLocation extends Location {
	
	public double tolerance;
	private double toleranceSquared;
	public TPLocation neededTarget = null;
	public ControlLevel spawnParticles;
	public ControlLevel playSound;	
	public boolean isFrom;

	public TPLocation(World world, double x, double y, double z, double tolerance, boolean isFrom) {
		super(world, x, y, z);
		this.tolerance = tolerance;
		this.toleranceSquared = tolerance * tolerance;
		this.isFrom = isFrom;
	}
	
	public boolean isLocAccepted(Location loc) throws IllegalArgumentException {
		//no need to check for world equality, this needs to be fast and the locations are stored in a LinkedHashMap with their world as key
		return this.distanceSquared(loc) <= this.toleranceSquared;
	}
	
	public String toString() {
		return this.getWorld().getName() + "," + this.getBlockX() + "," + this.getBlockY() + "," + this.getBlockZ() + "," + this.tolerance; 
	}
	
	private final String clrCmd = ChatColor.AQUA.toString();
	private final String clrDesc = ChatColor.WHITE.toString();
	private final String clrHead = ChatColor.YELLOW.toString();
	
	public String toCString() {
		return "{" + clrCmd + (this.isFrom ? this.toString() : "*") + clrDesc + ", " 
				+ clrCmd + (this.isFrom ? (this.neededTarget != null ? this.neededTarget.toString() : "*") : this.toString()) + clrDesc + ", " 
				+ clrCmd + this.spawnParticles.toString() + clrDesc + ", " 
				+ clrCmd + this.playSound.toString() + clrDesc + "}";
	}
	
	public String toCStringFromMarked() {
		return "{" + clrHead + (this.isFrom ? this.toString() : "*") + clrDesc + ", " 
				+ clrCmd + (this.isFrom ? (this.neededTarget != null ? this.neededTarget.toString() : "*") : this.toString()) + clrDesc + ", " 
				+ clrCmd + this.spawnParticles.toString() + clrDesc + ", " 
				+ clrCmd + this.playSound.toString() + clrDesc + "}";
	}
	
	public String toCStringToMarked() {
		return "{" + clrCmd + (this.isFrom ? this.toString() : "*") + clrDesc + ", " 
				+ clrHead + (this.isFrom ? (this.neededTarget != null ? this.neededTarget.toString() : "*") : this.toString()) + clrDesc + ", " 
				+ clrCmd + this.spawnParticles.toString() + clrDesc + ", " 
				+ clrCmd + this.playSound.toString() + clrDesc + "}";
	}

	public boolean dataEquivalent(TPLocation location) {
		return this.getWorld() == location.getWorld() && 
			   this.getX() == location.getX() && 
			   this.getY() == location.getY() && 
			   this.tolerance == location.tolerance && 
			   this.isFrom == location.isFrom && 
			   this.spawnParticles == location.spawnParticles && 
			   this.playSound == location.playSound;
	}
}

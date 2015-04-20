package mathe172.minecraft.plugins.TeleParticles;

import java.util.Random;

import mathe172.minecraft.plugins.TeleParticles.Config.ControlLevel;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TPListener implements Listener {
	
	private TeleParticles plugin;
	
	public TPListener(TeleParticles instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if ((event.getTo() == null) || (event.getFrom() == null) || (event.getPlayer() == null)) {
			return;
		}
		Player p = event.getPlayer();

		p = Config.disableEffects(p);
		if (p == null) return;
		
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN && Config.ignoreUnknown) return;
		
		Location from = event.getFrom().clone();
		final Location to = event.getTo().clone();

		boolean spawnParticles = p.hasPermission("teleparticles.particles");
		boolean playSound = p.hasPermission("teleparticles.sounds");
		
		boolean forceParticles = false;
		boolean forceSound = false;
		boolean denyParticles = false;
		boolean denySound = false;
		
		if (event.getCause() == TeleportCause.COMMAND && plugin.players.containsKey(p.getName())) {
			TPCmdIssuerData issuerData = plugin.players.get(p.getName());
			if (System.currentTimeMillis() < issuerData.issueTimestamp + Config.maxDelay) {
				forceParticles = issuerData.spawnParticles == ControlLevel.force || forceParticles;
				forceSound = issuerData.playSound == ControlLevel.force || forceSound;
				denyParticles = issuerData.spawnParticles == ControlLevel.deny || denyParticles;
				denySound = issuerData.playSound == ControlLevel.deny || denySound;
			}
			plugin.players.remove(p.getName());
		}
		
		String fromWorldName = event.getFrom().getWorld().getName();
		String toWorldName = event.getTo().getWorld().getName();
		
		if (Config.fromLocationsWithTargets.containsKey(fromWorldName)) {
			for (TPLocation location : Config.fromLocationsWithTargets.get(fromWorldName)) {
				if (location.isLocAccepted(from) && location.neededTarget.isLocAccepted(to)) {
					forceParticles = location.spawnParticles == ControlLevel.force || forceParticles;
					forceSound = location.playSound == ControlLevel.force || forceSound;
					denyParticles = location.spawnParticles == ControlLevel.deny || denyParticles;
					denySound = location.playSound == ControlLevel.deny || denySound;					
				}
			}
		}
		if (Config.fromLocationsWithoutTargets.containsKey(fromWorldName)) {
			for (TPLocation location : Config.fromLocationsWithoutTargets.get(fromWorldName)) {
				if (location.isLocAccepted(from)) {
					forceParticles = location.spawnParticles == ControlLevel.force || forceParticles;
					forceSound = location.playSound == ControlLevel.force || forceSound;
					denyParticles = location.spawnParticles == ControlLevel.deny || denyParticles;
					denySound = location.playSound == ControlLevel.deny || denySound;					
				}
			}
		}
		if (Config.toLocations.containsKey(toWorldName)) {
			for (TPLocation location : Config.toLocations.get(toWorldName)) {
				if (location.isLocAccepted(to)) {
					forceParticles = location.spawnParticles == ControlLevel.force || forceParticles;
					forceSound = location.playSound == ControlLevel.force || forceSound;
					denyParticles = location.spawnParticles == ControlLevel.deny || denyParticles;
					denySound = location.playSound == ControlLevel.deny || denySound;					
				}
			}
		}

		playSound = !denySound && (forceSound || playSound);
		spawnParticles = !denyParticles && (forceParticles || spawnParticles);
		
		if (playSound) {
			
			if ((from.getWorld().equals(to.getWorld()))) {
				if (from.distanceSquared(to) >= Config.minSoundDistanceSquared) {
					from.getWorld().playSound(from, Sound.ENDERMAN_TELEPORT, 0.3f, 1f);
						
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					    @Override 
					    public void run() {
					    	to.getWorld().playSound(to, Sound.ENDERMAN_TELEPORT, 0.3f, 1f);
					    }
					}, ((from.distance(to) >= 5) ? 1L : 0L));
				}
			} else {
				from.getWorld().playSound(from, Sound.ENDERMAN_TELEPORT, 0.3f, 1f);
				
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				    @Override 
				    public void run() {
				    	to.getWorld().playSound(to, Sound.ENDERMAN_TELEPORT, 0.3f, 1f);
				    }
				}, 1L);
			}
		}
		
		if (spawnParticles) {

			if (!(from.getWorld().equals(to.getWorld())) || (from.distanceSquared(to) >= Config.minParticleDistanceSquared)) {
					
				spawnSmoke(from, 5);
				spawnSmoke(from.add(0,1,0), 5);
				from.getWorld().playEffect(from, Effect.MOBSPAWNER_FLAMES, null);		
				
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				    @Override 
				    public void run() {
				    	for (int i = 0; i<3 ; i++) {
							to.getWorld().playEffect(to, Effect.ENDER_SIGNAL, null);
							to.getWorld().playEffect(to, Effect.ENDER_SIGNAL, null);
							to.add(0, 1, 0);
						}
				    }
				}, 1L);
			}
		}
	}
	public static Random random = new Random();

	public static void spawnSmoke(Location location, float thickness) {
		int singles = (int) Math.floor(thickness*9);
		for (int i = 0; i < singles; i++)
		{
			if (location == null) return;
			location.getWorld().playEffect(location, Effect.SMOKE, random.nextInt(9));
		}
	}
}

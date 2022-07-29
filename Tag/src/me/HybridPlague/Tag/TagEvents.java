package me.HybridPlague.Tag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TagEvents implements Listener {

	private Tag plugin;
	public TagEvents(Tag plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onTag(EntityDamageByEntityEvent e) {
		
		if (!plugin.game.isPlaying()) return;
		
		if (!(e.getEntity() instanceof Player)) return;
		if (!(e.getDamager() instanceof Player)) return;
		
		if (!e.getEntity().getWorld().getName().equals("Tag")) return;
		
		if (!plugin.game.getQueued().contains(((Player) e.getDamager()))) return;
		
		try {
			e.setCancelled(true);
			
			if ((Player) e.getDamager() != plugin.game.getIt()) return;
			
			if (((Player) e.getEntity()) == plugin.game.getLastTagged()) {
				((Player) e.getDamager()).sendMessage(plugin.prefix + ChatColor.translateAlternateColorCodes('&', "&cNo tag-backs!"));
				return;
			}
			
			((Player) e.getDamager()).setGlowing(false);
			
			plugin.game.setLastTagged((Player) e.getDamager());
			
			plugin.game.tagged((Player) e.getEntity());
			
			e.getEntity().getWorld().spawnParticle(Particle.FLASH, e.getEntity().getLocation(), 0);
			e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 10);
			
			Player p = (Player) e.getEntity();
			
			for (OfflinePlayer op : plugin.game.getQueued()) {
				((Player) op).sendMessage(plugin.prefix + ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + " &ais it!"));
			}
		} catch (NullPointerException ex) {
			//
		}
			
	}
	
	@EventHandler
	public void worldChange(PlayerChangedWorldEvent e) {
		
		if (!e.getFrom().getName().equals("Tag")) return;
		
		if (!plugin.game.isQueue()) return; // no party in session
		if (!plugin.game.getQueued().contains(e.getPlayer())) return; // not in the party
		
		if (plugin.game.getIt() == e.getPlayer()) { // player is it
			e.getPlayer().setGlowing(false);
		
			if (plugin.game.getQueued().size() < 3) { // not enough players
				plugin.game.endGame();
				return;
			}
			
			plugin.game.setQuiter((OfflinePlayer) e.getPlayer());
			
			Bukkit.getScheduler().runTaskLater(plugin, 
					new Runnable() {
						public void run() {
							plugin.game.tagged((Player) plugin.game.pickNewit());
						}
			}, 10);
			return;
		}
		
		plugin.game.removeQueued(e.getPlayer());
		
		if (plugin.game.isPlaying()) { // there is a game in session
			if (plugin.game.getQueued().size() < 3) { // not enough players
				plugin.game.endGame();
				return;
			}
		}
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		
		if (!plugin.game.isQueue()) return; // no party in session
		if (!plugin.game.getQueued().contains(e.getPlayer())) return; // not in the party
		
		if (plugin.game.getIt() == e.getPlayer()) { // player is it
			e.getPlayer().setGlowing(false);
		
			if (plugin.game.getQueued().size() < 3) { // not enough players
				plugin.game.endGame();
				return;
			}
			
			plugin.game.setQuiter((OfflinePlayer) e.getPlayer());
			
			Bukkit.getScheduler().runTaskLater(plugin, 
					new Runnable() {
						public void run() {
							plugin.game.tagged((Player) plugin.game.pickNewit());
						}
			}, 10);
			return;
		}
		
		plugin.game.removeQueued(e.getPlayer());
		
		if (plugin.game.isPlaying()) { // there is a game in session
			if (plugin.game.getQueued().size() < 3) { // not enough players
				plugin.game.endGame();
				return;
			}
		}
		
		return;
	}
	
}

package me.HybridPlague.Tag;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import me.HybridPlague.Tag.models.Game;

public class Tag extends JavaPlugin {

	public Game game;
	public String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&4Tag&7] &f");
	
	@Override
	public void onEnable() {
		
		this.game = new Game();
		
		this.getServer().getPluginManager().registerEvents(new TagEvents(this), this);
		
		this.getCommand("tag").setExecutor(new TagCommand(this));
		this.getCommand("ptag").setExecutor(new TagCommand(this));
	}
	
	@Override
	public void onDisable() {
		
		if(game.isPlaying()) game.end();
		
	}
}

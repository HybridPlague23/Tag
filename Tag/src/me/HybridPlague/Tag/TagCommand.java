package me.HybridPlague.Tag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand implements CommandExecutor {

	private Tag plugin;
	public TagCommand(Tag plugin) {
		this.plugin = plugin;
	}
	
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (!p.getWorld().getName().equals("Tag")) {
				p.sendMessage(ChatColor.RED + "This plugin does not work in this world!");
				return true;
			}
			
			switch (args.length) {
			case 1:
				if (args[0].equalsIgnoreCase("kick")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					p.sendMessage(plugin.prefix + ChatColor.RED + "/tag kick <player>");
					return true;
				}
				if (args[0].equalsIgnoreCase("leave")) {
					if (!plugin.game.isQueue()) {
						p.sendMessage(plugin.prefix + ChatColor.RED + "You are not in the party.");
						return true;
					}
					if (plugin.game.isQueue()) {
						if (!plugin.game.isPlaying()) { // queue exists
							if (!plugin.game.getQueued().contains(p)) { // not in the queue
								p.sendMessage(plugin.prefix + ChatColor.RED + "You are not in the party.");
								return true;
							}
							plugin.game.removeQueued(p);
							p.sendMessage(plugin.prefix + ChatColor.RED + "You have left the party.");
							
							if (plugin.game.isPlaying()) { // game in session
								if (plugin.game.getQueued().size() < 3) { // not enough players
									
									for (OfflinePlayer op : plugin.game.getQueued()) {
										((Player) op).sendMessage(plugin.prefix + ChatColor.RED + "The game has ended due to not enough players");
									}
									plugin.game.endGame();
									return true;
								}
								if (plugin.game.getIt() == p) { // player was IT
									plugin.game.setQuiter(p);
									
									Bukkit.getScheduler().runTaskLater(plugin, 
											new Runnable() {
												public void run() {
													plugin.game.tagged((Player) plugin.game.pickNewit());
												}
									}, 10);
								}
								return true;
							}
							return true;
						}
					}
					
				}
				if (args[0].equalsIgnoreCase("join")) {
					if (!plugin.game.isQueue()) {
						p.sendMessage(plugin.prefix + ChatColor.RED + "No party in session!");
						return true;
					}
					if (plugin.game.getHost() == p) {
						p.sendMessage(plugin.prefix + ChatColor.RED + "You are already in the party!");
						return true;
					}
					plugin.game.addQueued(p);
					p.sendMessage(plugin.prefix + ChatColor.GREEN + "You have joined the party!");
					return true;
				}
				if (args[0].equalsIgnoreCase("start")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					p.sendMessage(plugin.prefix + ChatColor.RED + "/tag start <game | party>");
					return true;
				}
				if (args[0].equalsIgnoreCase("end")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					p.sendMessage(plugin.prefix + ChatColor.RED + "/tag end <game | party>");
					return true;
				}
				if (args[0].equalsIgnoreCase("list")) {
					plugin.game.queueList(p);
					return true;
				}
			case 2:
				if (args[0].equalsIgnoreCase("kick")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					if (!plugin.game.isQueue()) {
						p.sendMessage(plugin.prefix + ChatColor.RED + "No game in session!");
						return true;
					}
					if (plugin.game.getHost() != p) { // not the host
						p.sendMessage(plugin.prefix + ChatColor.RED + "Only the host can end the game!");
						return true;
					}
					if (!plugin.game.getQueued().contains(Bukkit.getOfflinePlayer(args[1]))) {
						p.sendMessage(plugin.prefix + ChatColor.RED + "Player not found.");
						return true;
					}
					plugin.game.kick(Bukkit.getOfflinePlayer(args[1]));
					if (Bukkit.getOfflinePlayer(args[1]).isOnline()) {
						((Player) Bukkit.getOfflinePlayer(args[1])).sendMessage(plugin.prefix + ChatColor.RED + "You were removed from the party by the host.");
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("end")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					if (args[1].equalsIgnoreCase("game")) {
						if (!plugin.game.isPlaying()) { // no game in session
							p.sendMessage(plugin.prefix + ChatColor.RED + "No game in session!");
							return true;
						}
						if (plugin.game.getHost() != p) { // not the host
							p.sendMessage(plugin.prefix + ChatColor.RED + "Only the host can end the game!");
							return true;
						}
						plugin.game.endGame();
						return true;
					}
					if (args[1].equalsIgnoreCase("party")) {
						if (!plugin.game.isQueue()) { // no party in session
							p.sendMessage(plugin.prefix + ChatColor.RED + "No party in session!");
							return true;
						}
						if (plugin.game.getHost() != p) { // not the host
							p.sendMessage(plugin.prefix + ChatColor.RED + "Only the host can end the game!");
							return true;
						}
						if (plugin.game.isPlaying()) { // there is a game in session
							p.sendMessage(plugin.prefix + ChatColor.RED + "There is a game in sessions! Please end it first.");
							return true;
						}
						plugin.game.endQueue();
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("start")) {
					if (!p.hasPermission("tag.admin")) {
						p.sendMessage(plugin.prefix + "You are not allowed to do that!");
						return true;
					}
					if (args[1].equalsIgnoreCase("game")) {
						if (plugin.game.isPlaying()) {
							p.sendMessage(plugin.prefix + ChatColor.RED + "A game is already started!");
							return true;
						}
						if (!plugin.game.isQueue()) {
							p.sendMessage(plugin.prefix + ChatColor.RED + "No queue in session!");
							return true;
						}
						if (plugin.game.getQueued().size() < 3) {
							p.sendMessage(plugin.prefix + ChatColor.RED + "Not enough players to start. Required: 3+");
							return true;
						}
						plugin.game.tagged((Player) plugin.game.pickFirstIt());
						p.sendMessage(plugin.prefix + ChatColor.GREEN + "Game Started");
						return true;
					}
					if (args[1].equalsIgnoreCase("party")) {
						if (plugin.game.isQueue()) {
							p.sendMessage(plugin.prefix + ChatColor.RED + "Party has already started");
							return true;
						}
						plugin.game.hosting(p);
						p.sendMessage(plugin.prefix + ChatColor.GREEN + "Party has started. Players can join using /tag join!");
						return true;
					}
				}
			default:
				help(p);
			}
			return true;
			
		}
		
		sender.sendMessage("No");
		return true;
	}
	
	private void help(Player p) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c-------< &f&lTag &c>-------"));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7• &c/tag start <game | party>"));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7• &c/tag end <game | party>"));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7• &c/tag kick <player>"));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7• &c/tag list"));
		p.sendMessage(ChatColor.RED + "---------------------");
	}
	
	
}

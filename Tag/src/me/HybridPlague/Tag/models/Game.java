package me.HybridPlague.Tag.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.HybridPlague.Tag.Tag;

public class Game {

	private Tag plugin;
	
	private String status;
	
	private int task; // runnables id
	
	private Player it;
	
	private Player lasttagged;
	
	private OfflinePlayer quit;
	
	private OfflinePlayer host;
	
	private boolean isPlaying;
	
	private boolean queue;
	
	private List<OfflinePlayer> queued = new ArrayList<>();
	
	public Game() {
		this.plugin = Tag.getPlugin(Tag.class);
		this.setTask(-1);
		this.setPlaying(false);
		this.setStatus(ChatColor.DARK_RED + "N/A");
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public Player getIt() {
		return it;
	}

	public void setIt(Player it) {
		this.it = it;
	}

	public int getTask() {
		return task;
	}

	public void setTask(int task) {
		this.task = task;
	}
	
	public OfflinePlayer getQuiter() {
		return quit;
	}

	public void setQuiter(OfflinePlayer quit) {
		this.quit = quit;
	}
	
	public Player getLastTagged() {
		return lasttagged;
	}
	
	public void setLastTagged(Player lasttagged) {
		this.lasttagged = lasttagged;
	}
	
	public boolean isQueue() {
		return queue;
	}
	
	public void setQueue(boolean queue) {
		this.queue = queue;
	}
	
	public OfflinePlayer getHost() {
		return host;
	}
	
	public void setHost(OfflinePlayer host) {
		this.host = host;
	}
	
	public void kick(OfflinePlayer op) {
		
		if (getIt() == op) {
			
			((Player) op).setGlowing(false);
			if (getQueued().size() < 3) { // not enough players
				endGame();
				return;
			}
			
			plugin.game.setQuiter(op);
			
			Bukkit.getScheduler().runTaskLater(plugin, 
					new Runnable() {
						public void run() {
							tagged((Player) pickNewit());
						}
			}, 10);
			return;
		}
		
		removeQueued(op);
		
		if (isPlaying()) { // there is a game in session
			if (getQueued().size() < 3) { // not enough players
				endGame();
				return;
			}
		}
		
		List<OfflinePlayer> list = this.queued;
		list.remove(op);
		this.queued = list;
		for (OfflinePlayer p : this.queued) {
			if (!p.isOnline());
			((Player) p).sendMessage(plugin.prefix + ChatColor.RED + op.getName() + " was removed from the queue.");
		}
	}
	
	public void addQueued(OfflinePlayer queued) {
		for (OfflinePlayer p : this.queued) {
			if (!p.isOnline());
			((Player) p).sendMessage(plugin.prefix + ChatColor.GREEN + queued.getName() + " has joined the queue!");
		}
		List<OfflinePlayer> list = this.queued;
		list.add(queued);
		this.queued = list;
	}
	
	public void removeQueued(OfflinePlayer queued) {
		List<OfflinePlayer> list = this.queued;
		list.remove(queued);
		this.queued = list;
		
		for (OfflinePlayer p : this.queued) {
			if (!p.isOnline());
			((Player) p).sendMessage(plugin.prefix + ChatColor.RED + queued.getName() + " has left!");
		}
	}
	
	public List<OfflinePlayer> getQueued() {
		return queued;
	}
	
	public OfflinePlayer pickNewit() {
		OfflinePlayer p = queued.stream().skip((int)
				(queued.size() * Math.random())).findFirst().orElse(null);
		
		for (OfflinePlayer pl : this.queued) {
			if (!pl.isOnline());
			((Player) pl).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix + " &e" + quit.getName() + " &awas it and has left. &e" + p.getName() + " &ais now it!"));
		}
		
		return p;
	}
	
	public OfflinePlayer pickFirstIt() {
		setStatus(ChatColor.GREEN + "Started");
		OfflinePlayer p = queued.stream().skip((int)
				(queued.size() * Math.random())).findFirst().orElse(null);
		for (OfflinePlayer pl : this.queued) {
			if (!pl.isOnline());
			((Player) pl).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix + " &aGame has started! &e" + p.getName() + " &ais it."));
		}
		return p;
	}
	
	public Scoreboard getBoard() {
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Objective obj = board.registerNewObjective("tagScoreboard", "dummy", 
				ChatColor.translateAlternateColorCodes('&', "&a&lSeconds Left: "));
		
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score score = obj.getScore(ChatColor.GOLD + it.getName());
		
		score.setScore(0);
		
		return board;
	}
	
	public void endGame() { // End the game
		
		lasttagged = null;
		setPlaying(false);
		setStatus(ChatColor.YELLOW + "In Queue");
		
		Bukkit.getScheduler().cancelTask(getTask());
		
		it.sendMessage(plugin.prefix + ChatColor.RED + "You lost!");
		it.setGlowing(false);
		
		for (OfflinePlayer p : queued) {
			((Player) p).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			if (((Player) p) != it) {
				((Player) p).sendMessage(ChatColor.GREEN + "You won!");
			}
		}
		
	}
	
	public void endQueue() { // End the queue
		
		for (OfflinePlayer p : queued) {
			if (!p.isOnline());
			((Player) p).sendMessage(plugin.prefix + ChatColor.translateAlternateColorCodes('&', "The party has been disbanded."));
		}
		
		setQueue(false);
		setHost(null);
		queued.clear();
		setStatus(ChatColor.DARK_RED + "N/A");
		
	}
	
	public void end() { // Force-end both queue and game
		
		lasttagged = null;
		setPlaying(false);
		setStatus(ChatColor.DARK_RED + "N/A");
		setHost(null);
		setQueue(false);
		this.queued.clear();
		
		Bukkit.getScheduler().cancelTask(getTask());
		
		it.sendMessage(plugin.prefix + ChatColor.RED + "You lost!");
		it.setGlowing(false);
		
		for (OfflinePlayer p : queued) {
			((Player) p).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			if (((Player) p) != it) {
				((Player) p).sendMessage(ChatColor.GREEN + "You won!");
			}
		}
		
		
	}
	
	public void queueList(Player p) {
		List<String> playing = new ArrayList<>();
		try {
			for (OfflinePlayer queued : this.queued) {
				if (!isPlaying()) { // Game hasn't starting, still in queue
					if (queued.equals(getHost())) {
						
					} else {
						playing.add(ChatColor.WHITE + queued.getName());
					}
				} else { // Game has started
					if (queued == host);
					if (it == (Player) queued) { // player is it, mark them red
						playing.add(ChatColor.DARK_RED + queued.getName());
					} else { // Not it, mark them green
						playing.add(ChatColor.GREEN + queued.getName());
					}
				}
			}
		} catch (NullPointerException ex) {
			// there is no queue started, so avoid an internal error/null error in console
		}
		
		String result = String.join("&7, &r", playing);
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c-------< &f&lTag &c>-------"));
		p.sendMessage("");
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &7Status: " + this.getStatus()));
		p.sendMessage("");
		if (this.getStatus().contains("Started") || this.getStatus().contains("In Queue")) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &7Host: &c" + this.getHost().getName()));
			p.sendMessage("");
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &7Players (" + playing.size() + "): " + result));
			p.sendMessage("");
		}
		p.sendMessage(ChatColor.RED + "---------------------");
		
	}
	
	public void hosting(OfflinePlayer p) {
		
		setHost(p);
		setQueue(true);
		setStatus(ChatColor.YELLOW + "In Queue");
		addQueued(p);
		
	}
	
	public void tagged(Player p) {
		setPlaying(true);
		
		if (getTask() != -1) {
			Bukkit.getScheduler().cancelTask(task);
		}
		
		setIt(p);
		it.setGlowing(true);
		
		for (OfflinePlayer online : queued) {
			((Player) online).setScoreboard(getBoard());
		}
		
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, 
				new Runnable() {
			
				int timeleft = 300;
				
				public void run() {
					if (timeleft <= 0) {
						end();
						return;
					}
					try {
						for (OfflinePlayer online : queued) {
							if (!online.isOnline());
							((Player) online).getScoreboard().getObjective(DisplaySlot.SIDEBAR)
							.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lSeconds Left: &c" + timeleft));
						}
					} catch (NullPointerException ex) {
						
					}
					
					timeleft--;
					
				}
			
	   // }, <when to start>, <when to repeat>
		}, 0, 20);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

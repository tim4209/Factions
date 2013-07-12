package com.gmail.tim98760.factions360;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Factions360 extends JavaPlugin {
	
	public Scoreboard board;
	public HashMap<String, String> invites = new HashMap<String, String>();
	
	public void onEnable() {
		initConfiguration();
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		for (Player player : getServer().getOnlinePlayers()) {
			setTeam(player);
		}
	}
	
	public void initConfiguration() {
		File configFile = new File(getDataFolder() + "/config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		reloadConfig();
	}
	
	public void onDisable() {
	}
	
	public String getTeam(String playerName) {
		return getConfig().getString("Players." + playerName + ".Faction");
	}

	public String getOwner(String factionName) {
		return getConfig().getString("Factions." + factionName + ".Owner");
	}
	
	public boolean isFaction(String factionName) {
		return getConfig().contains("Factions." + factionName);
	}
	
	public boolean isLeader(String playerName, String factionName) {
		String owner = getConfig().getString("Factions." + factionName + ".Owner");
		return playerName.equalsIgnoreCase(owner);
	}
	
	public void setTeam(final Player player) {
		final String teamName = getTeam(player.getName().toLowerCase());
		if (teamName == null || teamName.isEmpty()) {
			return;
		}
		Team team = board.getTeam(teamName);
		if (team == null){
			team = board.registerNewTeam(teamName);
		}
		team.addPlayer(player);
		team.setAllowFriendlyFire(false);
		player.setScoreboard(board);
	}
	
	public void removeTeam(final Player player) {
		final String teamName = getTeam(player.getName().toLowerCase());
		if (teamName == null || teamName.isEmpty()) {
			return;
		}
		Team team = board.getTeam(teamName);
		if (team == null){
			return;
		}
		team.removePlayer(player);
		player.setScoreboard(board);
	}
	
	public void joinTeam(String playerName, String teamName) {
		getConfig().set("Players." + playerName + ".Faction", teamName);
		saveConfig();
	}
	
	public void quitTeam(String playerName) {
		getConfig().set("Players." + playerName, null);
		saveConfig();
	}
	
	public void createTeam(String teamName, String ownerName) {
		getConfig().set("Factions." + teamName + ".Owner", ownerName);
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("F")) {
        	if (args.length == 0) {
        		sender.sendMessage("/F Create - Create a new Faction.");
        		sender.sendMessage("/F Info - Get your factions info.");
        		sender.sendMessage("/F Invite - Send a invite to your faction.");
        		sender.sendMessage("/F Quit - Quit your faction.");
        		sender.sendMessage("/F Yes - Accept a faction invite.");
        		sender.sendMessage("/F No - Decline a faction invite.");
        		return true;
        	}
    		if (sender instanceof Player) {
    			Player player = (Player) sender;
    			String playerName = player.getName().toLowerCase();
    			String teamName = getTeam(playerName);
				if((args.length >= 2) && (args[0].equalsIgnoreCase("Create"))) {
					if (teamName == null || teamName.isEmpty()) {
						if(isFaction(args[1])) {
							player.sendMessage("Sorry! That faction name is used allready.");
							return true;
						}
						player.sendMessage("You have successfuly created: " + ChatColor.RED +  args[1]);
						createTeam(args[1], playerName);
						joinTeam(playerName, args[1]);
						setTeam(player);
						return true;
					}
					player.sendMessage("You are allready in a faction!");
					return true;
				}
				
				if((args.length >= 1) && (args[0].equalsIgnoreCase("Info"))) {
					if (teamName == null || teamName.isEmpty()) {
						player.sendMessage("You dont have a faction!");
						return true;
					}
					player.sendMessage("Faction Name: " + teamName);
					player.sendMessage("Faction Owner: " + getOwner(teamName));
					return true;
				}
				
				if((args.length >= 1) && (args[0].equalsIgnoreCase("No"))) {
					if (teamName == null || teamName.isEmpty()) {
						if (invites.containsKey(playerName)) {
							invites.remove(playerName);
							player.sendMessage("You dont have a pending faction invite!");
							return true;
						}
						player.sendMessage("You dont have a pending faction invite!");
						return true;
					}
					player.sendMessage("You are allready in a faction!");
					return true;
				}
				
				if((args.length >= 1) && (args[0].equalsIgnoreCase("Yes"))) {
					if (teamName == null || teamName.isEmpty()) {	
						if (invites.containsKey(playerName)) {
							String newTeam = invites.get(playerName);
							if (newTeam == null || newTeam.isEmpty()) {
								player.sendMessage("Could not join null faction!");
								return true;
							}
							invites.remove(playerName);
							joinTeam(playerName, newTeam);
							setTeam(player);
							player.sendMessage("You have succesfully joined: " + newTeam);
							return true;
						}
						player.sendMessage("You dont have a pending faction invite!");
						return true;
					}
					player.sendMessage("You are allready in a faction!");
					return true;
				}
				
				if((args.length >= 1) && (args[0].equalsIgnoreCase("quit"))) {
					if (teamName == null || teamName.isEmpty()) {
						player.sendMessage("You dont have a faction!");
						return true;
					}
					removeTeam(player);
					quitTeam(playerName);
					player.sendMessage("You have quit your faction");
					return true;
				}
				
				if((args.length >= 2) && (args[0].equalsIgnoreCase("Invite"))) {
					if (teamName == null || teamName.isEmpty()) {
						player.sendMessage("You dont have a faction!");
						return true;
					}
					if(isLeader(playerName,teamName)) {
						Player target = sender.getServer().getPlayer(args[1]);
						if (target == null) {
							player.sendMessage(args[1] + " could not be found!");
							return true;
						}
						String targetName = target.getName().toLowerCase();
						String targetteamName = getTeam(targetName);
						if (targetteamName == null || targetteamName.isEmpty()) {
							if (invites.containsKey(targetName)) {
								player.sendMessage(targetName + " allready has a pending faction invite!");
								target.sendMessage("You have ben invited to a faction but you allready have a pending request");
								target.sendMessage("To accept it type /f yes!");
								target.sendMessage("To deny it type /f no!");
								return true;
							}
							invites.put(targetName, teamName);
							player.sendMessage(targetName + " has ben invited to the faction!");
							target.sendMessage("You have ben invited to " + teamName);
							target.sendMessage("To accept it type /f yes!");
							target.sendMessage("To deny it type /f no!");
							return true;
						}
						player.sendMessage(targetName + " is allready in a faction!");
						return true;
					}
					player.sendMessage("Only a leader can invite people!");
					return true;
				}
    		}
        }
        return true;
	}

}

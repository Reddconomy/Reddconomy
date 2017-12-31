// This enables redd's exchanges in-game

package net.blockstreet.coin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class Exchange extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		// TODO what the plugin will do when enabled
		getServer().getPluginManager().registerEvents(this, this);
		//getLogger().info("onEnable has been invoked!");
		getLogger().info("Exchanges between users, activated!");
	}
	
	@Override
	public void onDisable() {
		// TODO what the plugin will do when disabled
		//getLogger().info("onDisable has been invoked!");
		getLogger().info("Exchanges between users, deactivated!");
	}
	
	// This does things at the player's first login.
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
	  if(!event.getPlayer().hasPlayedBefore()) {
		  //do things
	    }
	}


	// This does commands.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Controllo iniziale prima del comando
		
	    if (sender instanceof Player) {
	        Player playerD = Bukkit.getPlayer(args[0]); // Player2
	        	sender.sendMessage("Say things to the sender.");
	        	playerD.sendMessage("Say things to the other player.");
	    } else {
	        sender.sendMessage("You must be a player!");
	        return false;
	    }
	    if (args.length>2 || args.length<2)
	    {
	    		sender.sendMessage("Wrong arguments! Check how to use this command here:");
	    		return false;
	    }

	    return false;
	}
}

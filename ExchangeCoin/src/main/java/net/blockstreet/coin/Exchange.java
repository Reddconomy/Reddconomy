package net.blockstreet.coin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
	  if(!event.getPlayer().hasPlayedBefore()) {
	    String fileName = (event.getPlayer().getDisplayName() + ".txt");
		    try {
	            // Assume default encoding.
	            FileWriter fileWriter =
	                new FileWriter(fileName);
	
	            // Always wrap FileWriter in BufferedWriter.
	            BufferedWriter bufferedWriter =
	                new BufferedWriter(fileWriter);
	
	            // Note that write() does not automatically
	            // append a newline character.
	            bufferedWriter.write("0");
	
	            // Always close files.
	            bufferedWriter.close();
	            event.getPlayer().sendMessage("Now you have 0 Reddcoins, you can exchange Reddcoins with /rsend");
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error writing to file '"
	                + fileName + "'");
	            // Or we could just do this:
	            // ex.printStackTrace();
	        }
	    }
	}
	
    public void findFile(String name,File file)
    {
        File[] list = file.listFiles();
        if(list!=null)
        for (File fil : list)
        {
            if (fil.isDirectory())
            {
                findFile(name,fil);
            }
            else if (name.equalsIgnoreCase(fil.getName()))
            {
                System.out.println(fil.getParentFile());
            }
        }
    }

	// Il comando si esegue con /rsend nomeplayer soldi
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Controllo iniziale prima del comando
		
	    if (sender instanceof Player) {
	        Player player = (Player) sender;
	        Player playerD = Bukkit.getPlayer(args[0]);
	        try {    
	        		sender.sendMessage("You don't have enough Reddcoins, you need " + (Integer.parseInt(args[1])-Integer.parseInt(lineM)) + " more Reddcoins.");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				sender.sendMessage("Player not found, check what you've wrote.");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				sender.sendMessage("Generic error, contact staff.");
				e.printStackTrace();
			}
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

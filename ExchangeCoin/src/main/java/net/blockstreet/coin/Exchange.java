// This enables redd's exchanges in-game

package net.blockstreet.coin;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	public void onPlayerJoinEvent(PlayerJoinEvent event) throws Throwable {
	  if(!event.getPlayer().hasPlayedBefore()) {

	    }
	}

	 private String getAddr() throws Exception {
		 
		  String urlString = "http://localhost:8099/getaddr";
		  
		  URL url = new URL(urlString);
		  HttpURLConnection con = (HttpURLConnection) url.openConnection();
		 
		  // By default it is GET request
		  con.setRequestMethod("GET");
		 
		  int responseCode = con.getResponseCode();
		  System.out.println("Sending get request : "+ url);
		  System.out.println("Response code : "+ responseCode);
		 
		  // Reading response from input Stream
		  BufferedReader in = new BufferedReader(
		          new InputStreamReader(con.getInputStream()));
		  String output;
		  StringBuffer response = new StringBuffer();
		 
		  while ((output = in.readLine()) != null) {
		   response.append(output);
		  }
		  in.close();
		  
		  
		  
		  return response.toString();
	 }
	 
	// This does commands.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		// Controllo iniziale prima del comando
		if (cmd.getName().equalsIgnoreCase("deposit"))
		
		    if (sender instanceof Player) {
			     /*   //Player playerD = Bukkit.getPlayer(args[0]); // Player2
				  JsonRpcHttpClient client = null;
				try {
					client = new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  String addr = null;
				try {
					addr = (String)client.invoke("getnewaddress",new Object[]{},Object.class);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			        	try {
							sender.sendMessage("Deposit to this address: " + getAddr());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	return true;
		    } else {
			        sender.sendMessage("You must be a player!");
			        return false;
		    }
		
		   /* if (args.length>0)
		    {
		    		sender.sendMessage("Wrong arguments! Check how to use this command here:");
		    		return false;
		    }*/
	
	
	    if (cmd.getName().equalsIgnoreCase("balance"))
		if (sender instanceof Player) {
			sender.sendMessage("Say balance");
			return true;
		} else {
			sender.sendMessage("You must be a player!");
			return false;
		}
	    
	    if (cmd.getName().equalsIgnoreCase("send"))
	    if (sender instanceof Player) {
	    	sender.sendMessage("Send money");
	    	return true;
	    } else {
	    	sender.sendMessage("You must be a player!");
	    	return false;
	    }
	    
	
	return false;
	}
	}
	
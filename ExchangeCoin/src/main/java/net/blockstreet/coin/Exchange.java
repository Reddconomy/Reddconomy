// This enables redd's exchanges in-game

package net.blockstreet.coin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
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
	
	final Gson _JSON;
	
	public Exchange() {
		_JSON = new GsonBuilder().setPrettyPrinting().create();
	}
	
	private Map apiCall(String action) throws Exception
	{
		  String urlString = "http://localhost:8099/?action=" + action;
		  URL url = new URL(urlString);
		  HttpURLConnection con = (HttpURLConnection) url.openConnection();
		 
		  // Reading response from input Stream
		  BufferedReader in = new BufferedReader(
		          new InputStreamReader(con.getInputStream()));
		  String output;
		  StringBuffer response = new StringBuffer();
		 
		  while ((output = in.readLine()) != null) {
		   response.append(output);
		  }
		  in.close();
		  
		  Map resp=_JSON.fromJson(response.toString(),Map.class);
		  return resp;
	}
	

	 private String getAddrDeposit(long balance, UUID pUUID) throws Exception {
		 
		  String action = "deposit&wallid=" + pUUID + "&balance=" + balance;
		  Map data=(Map)apiCall(action).get("data");
		  return (String)data.get("addr");
	 }
	 
	 private long getBalance(UUID pUUID) throws Exception {
		 
		  String action = "balance&wallid=" + pUUID;
		  Map data=(Map)apiCall(action).get("data");
		  Number balance=(Number)data.get("balance");
		  return balance.longValue();
	 }
	 
	// This does commands.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		// Controllo iniziale prima del comando
	    	if (!(sender instanceof Player))
	    	{
		    	sender.sendMessage("You must be a player!");
		    	return false;
	    	}
	    	
		if (cmd.getName().equalsIgnoreCase("deposit"))
		{
		    if (args.length==1) {
			    	UUID pUUID = ((Player) sender).getUniqueId();
			    	long balance = (long)(Double.parseDouble(args[0])*100000000L);
				        	try {
								sender.sendMessage("Deposit to this address: " + getAddrDeposit(balance, pUUID));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        	return true;
		    } else {
	    			sender.sendMessage("Wrong arguments! Check how to use this command here:");
	    			return false;
		    }
		}
	
		else if (cmd.getName().equalsIgnoreCase("balance"))
	    {		    	
		    if (args.length==0)
		    	{
				UUID pUUID = ((Player) sender).getUniqueId();
				try {
					sender.sendMessage("You have: " + getBalance(pUUID) + " RDD");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			} else {
		    		sender.sendMessage("Wrong arguments! Check how to use this command here:");
		    		return false;
			}
	    }
	    
		else if (cmd.getName().equalsIgnoreCase("contract"))
	    {		    	
		    	if (args.length==2)
		    	{
			    	// Do stuff.
				// sender.sendMessage("Send money");
				return true;
			} else {
			    	sender.sendMessage("Wrong arguments! Check how to use this command here:");
			    	return false;
			}
	    }
	    return false;
	}
}	
// This enables reddCoin exchanges in-game
// Author: Simone Cervino, @soxasora
package net.reddconomy.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.event.player.PlayerJoinEvent;
//import java.util.UUID;

public class Exchange extends JavaPlugin implements Listener {
	
	// Let's declare ExchangeEngine, shall we?
	ExchangeEngine engine = new ExchangeEngine();
	
	// This is what the plugin do when activated
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Exchanges between users, activated!");
	}
	
	// And this is what the plugin do when deactivated.
	@Override
	public void onDisable() {
		getLogger().info("Exchanges between users, deactivated!");
	}
	
	// This should do things when a new player logins for the first time.
	// As you can see, it's deactivated because, well, we don't need it.
	/*@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) throws Throwable {
	  if(!event.getPlayer().hasPlayedBefore()) {

	    }
	}*/
	
	// Commands!
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// Maybe you're asking yourself why am I using String instead of UUID.
		// Well, let's say that you will find out later. Let's continue!
		String pUUID = null;
		
		// These commands only works if a player executes them. So here's a way to force it.
	    if (!(sender instanceof Player))
	    {
		    sender.sendMessage("You must be a player!");
		    return false;
	    }
	    
	    // Here we are. I'm using String instead of UUID because
	    // if a server is cracked (offline mode), pUUID will have the Player's name
	    // instead of the Mojang's UUID
	    // Extremely unsafe way to use this plugin, please, make only original servers.
	    if ((getServer().getOnlineMode())==true)
	    {
	    	pUUID = (((Player) sender).getUniqueId()).toString();
	    } else {
	    	pUUID = sender.getName();
	    }
	    	
	    // /deposit command here. | This shows the deposit address where the player
	    // 						  | should send money.
	    // example: /deposit 27.3 | You will deposit 27.3 RDD this way.
		if (cmd.getName().equalsIgnoreCase("deposit"))
		{
		    if (args.length==1) {
			    	long balance = (long)(Double.parseDouble(args[0])*100000000L);
				        	try {
								sender.sendMessage("Deposit to this address: " + engine.getAddrDeposit(balance, pUUID));
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
		
		// /balance just shows.. well, you guessed it! The balance!
		else if (cmd.getName().equalsIgnoreCase("balance"))
	    {		    	
		    if (args.length==0)
		    	{
				try {
					sender.sendMessage("You have: " + engine.getBalance(pUUID) + " RDD");
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
		
		// Let's just use it in order to test /deposit. Don't use it in a production server.
		else if (cmd.getName().equalsIgnoreCase("gettestcoins"))
		{
			if (args.length==2)
			{
				long ammount = (long)(Double.parseDouble(args[1])*100000000L);
				String addr = args[0];
				try {
					engine.getTestCoins(addr, ammount);
					sender.sendMessage("It worked!");
					sender.sendMessage("You added " + args[1] + " to the address: " + addr);
				} catch (Exception e) {
					sender.sendMessage("Nope. Well, at least I tried.. *badum tss*");
				}
			}
		}
	    
		// "send" is boring and unsafe. Let's make contracts!
		// TO-DO: a way to make it easier for the user.
		else if (cmd.getName().equalsIgnoreCase("contract"))
	    {		    	
			if (args.length==2)
		    	{
		    		if (args[0].equalsIgnoreCase("new"))
		    		{
		    			long ammount = (long)(Double.parseDouble(args[1])*100000000L);
		    			try {
							sender.sendMessage("Contract ID: " + engine.createContract(ammount, pUUID));
							sender.sendMessage("Contract created. Share the Contract ID.");
						} catch (Exception e) {
							sender.sendMessage("Cannot create contract. Call an admin for more info.");
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		} else if (args[0].equalsIgnoreCase("accept")) {
		    			String contractId = args[1];
		    			try {
		    				engine.acceptContract(contractId, pUUID);
							sender.sendMessage("Contract accepted.");
							sender.sendMessage("You now have: " + engine.getBalance(pUUID) + " RDD");
						} catch (Exception e) {
							sender.sendMessage("Cannot accept contract. Are you sure that you haven't already accepted?");
							sender.sendMessage("Otherwise, call and admin for more info.");
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}
				return true;
			} else {
			    	sender.sendMessage("Wrong arguments! Check how to use this command here:");
			    	return false;
			}
	    }
	    return false;
	}
}	
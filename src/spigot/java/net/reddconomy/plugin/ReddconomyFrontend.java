// This enables reddCoin exchanges in-game
// Author: Simone Cervino, @soxasora
package net.reddconomy.plugin;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

// Bukkit libraries
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.event.player.PlayerJoinEvent; // Remove if you want to use onPlayerJoinEvent

// Image in chat framework
import com.bobacadodl.imgmessage.ImageChar;
import com.bobacadodl.imgmessage.ImageMessage;

public class ReddconomyFrontend extends JavaPlugin implements Listener {
	
	private String EnableQR;
	private String coin;
	private String reddconomy_api_url=getConfig().getString("reddconomy_api_url");
	private boolean testmode;
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();

	// This is what the plugin does when activated
	@Override
	public void onEnable() {
		saveDefaultConfig();
		EnableQR=getConfig().getString("EnableQR");
		coin=getConfig().getString("coin");
		testmode=getConfig().getBoolean("testmode");
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Exchanges between users, activated!");
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				processPendingDeposits();
			}	
		}, 100, 200);
		
	  } 
	
	private void processPendingDeposits() {
		final Iterator<String> it=_PENDING_DEPOSITS.iterator();

		while(it.hasNext()){
			String addr=it.next();
		
			try{
				final PendingDepositData deposit_data=api.getDepositStatus(addr);
				final UUID pUUID=UUID.fromString(deposit_data.addr);
				if(deposit_data.status!=1){
					it.remove();
					Bukkit.getScheduler().runTaskLater(this,new Runnable(){
						@Override
						public void run() {
							Bukkit.getPlayer(pUUID).sendMessage(
									deposit_data.status==0?"Deposit completed. Check your balance!":"Deposit expired! Request another one."
							);
						}						
					},0);
				}		
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	// Let's declare ExchangeEngine, shall we?
	
	ReddconomyApi api = new ReddconomyApi(reddconomy_api_url);
	// And this is what the plugin does when deactivated.
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
	

	
	@SuppressWarnings({ "deprecation" })
	@EventHandler
	public void onInteract(PlayerInteractEvent Event) throws InterruptedException {

		  Player player = Event.getPlayer();
		  final Block interacted = Event.getClickedBlock();
		  if(interacted==null)return;
		  if(interacted.getState() instanceof Sign) {
			  BlockState sign = interacted.getState();
			  String line0 = ((Sign) sign).getLine(0);
			  String line1 = ((Sign) sign).getLine(1);
			  String line2 = ((Sign) sign).getLine(2);
			  //String line3 = ((Sign) sign).getLine(3);
			  UUID pUUID = player.getUniqueId();
			  UUID sellerUUID = (Bukkit.getServer().getPlayer(line1)).getUniqueId();
			  if (line0.equals("[CONTRACT]"))
			  {
				  
				  if (!line1.equalsIgnoreCase(Bukkit.getPlayer(pUUID).getDisplayName()) || getConfig().getBoolean("testmode")!=false)
				  {
		
					  	long amount = (long)(Double.parseDouble(line2)*100000000L);
						try {
							String cID = api.createContract(amount, sellerUUID);
							int status = api.acceptContract(cID, pUUID);
							if (status==200)
							{
							player.sendMessage("Contract ID: "+cID);
							player.sendMessage("Contract accepted. Redstone activated.");
							} else {
							//TODO sned error instead of this fake shit
							player.sendMessage("Cannot create contract");
							player.sendMessage("Maybe not enough money? Check your balance with /balance");
							}
							// this way, it powers button attached to the interacted sign.
							api.powerButton(interacted);
							Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								  @Override
								  public void run() {
									api.shutdownButton(interacted);
								  }
								}, 5);
								 
						} catch (Exception e) {
							player.sendMessage("Cannot create/accept contract. Call an admin for more info.");
							e.printStackTrace();
						}
		
				  } else {
					  player.sendMessage("You can't accept YOUR contracts.");
				  }
			  }
		  	}
	  }
	
	



	// Commands!
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// These commands only works if a player executes them. So here's a way to force it.
	    if (!(sender instanceof Player))
	    {
		    sender.sendMessage("You must be a player!");
		    return false;
	    }
	    
	    // Getting the unique ID of the sender.
	    UUID pUUID = ((Player) sender).getUniqueId();
	    	
	    // /deposit command here. | This shows the deposit address where the player
	    //                        | should send money.
	    // example: /deposit 27.3 | You will deposit 27.3 RDD this way.
		if (cmd.getName().equalsIgnoreCase("deposit"))
		{
		    if (args.length==1) {
			    	long balance = (long)(Double.parseDouble(args[0])*100000000L);
				        	try {
				        		String addr=api.getAddrDeposit(balance, pUUID);
				        		
				        			if (EnableQR.equalsIgnoreCase("enabled"))
				        			{
				        			BufferedImage bimg = api.QR(addr, coin, args[0]);
					        		new ImageMessage(
					        				bimg,
					        				bimg.getHeight(),
					        				ImageChar.BLOCK.getChar()
					        				).sendToPlayer(sender);
					        		sender.sendMessage("Deposit "+args[0]+" "+coin+" to this address: "+addr);
				        			} else if (EnableQR.equalsIgnoreCase("link")){
				        				//BufferedImage bimg = api.QR(addr, coin, args[0]);
				        				//ImageIO.write(bimg, "png", new File (addr+".png"));
				        				sender.sendMessage("Deposit "+args[0]+" "+coin+" to this address: "+addr);
				        			} else if (EnableQR.equalsIgnoreCase("disabled")) {
				        				sender.sendMessage("Deposit "+args[0]+" "+coin+" to this address: "+addr);
				        			}
				        			_PENDING_DEPOSITS.add(addr);
				        		
							} catch (Exception e) {
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
					sender.sendMessage("You have: " + api.getBalance(pUUID) + " RDD");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else {
		    		sender.sendMessage("Wrong arguments! Check how to use this command here:");
		    		return false;
			}
	    }
		
		// Let's just use it in order to test /deposit. Don't use it in a production server.
		else if (cmd.getName().equalsIgnoreCase("sendcoins"))
		{
			if (args.length==2)
			{
				long amount = (long)(Double.parseDouble(args[1])*100000000L);
				String addr = args[0];
				try {
					api.sendCoins(addr, amount);
					sender.sendMessage("It worked!");
					sender.sendMessage("You added " + args[1] + " to the address: " + addr);
				} catch (Exception e) {
					sender.sendMessage("Nope. Well, at least I tried.. *badum tss*");
				}
			} else {
		    	sender.sendMessage("Wrong arguments! Check how to use this command here:");
		    	return false;
			}
		}
		
		else if (cmd.getName().equalsIgnoreCase("withdraw"))
		{
			if (args.length==2)
			{
				long amount = (long)(Double.parseDouble(args[0])*100000000L);
				String addr = args[1];
				try {
					api.withdraw(amount, addr, pUUID);
					sender.sendMessage("Withdrawing..");
					sender.sendMessage("Ok, it should work, wait please.");
				} catch (Exception e) {
					sender.sendMessage("Something went wrong. Call an admin.");
				}
			} else {
				sender.sendMessage("Wrong arguments! Check how to use this command here:");
				return false;
			}
		}
	    
		// "send" is boring and unsafe. Let's make contracts!
		// TODO: a way to make it easier for the user.
		else if (cmd.getName().equalsIgnoreCase("contract"))
	    {		    	
			if (args.length==2)
		    	{
		    		if (args[0].equalsIgnoreCase("new"))
		    		{
		    			long amount = (long)(Double.parseDouble(args[1])*100000000L);
		    			try {
							sender.sendMessage("Contract ID: " + api.createContract(amount, pUUID));
							sender.sendMessage("Contract created. Share the Contract ID.");
						} catch (Exception e) {
							sender.sendMessage("Cannot create contract. Call an admin for more info.");
							e.printStackTrace();
						}
		    		} else if (args[0].equalsIgnoreCase("accept")) {
		    			String contractId = args[1];
		    			try {
		    				api.acceptContract(contractId, pUUID);
							sender.sendMessage("Contract accepted.");
							sender.sendMessage("You now have: " + api.getBalance(pUUID) + " RDD");
						} catch (Exception e) {
							sender.sendMessage("Cannot accept contract. Are you sure that you haven't already accepted?");
							sender.sendMessage("Otherwise, call and admin for more info.");
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
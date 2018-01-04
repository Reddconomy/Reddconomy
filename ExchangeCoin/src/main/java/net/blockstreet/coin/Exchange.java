// This enables reddcoin exchanges in-game
// Author: Simone Cervino, @soxasora
package net.blockstreet.coin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.event.player.PlayerJoinEvent;
//import java.util.UUID;

public class Exchange extends JavaPlugin implements Listener {
	
	// Dichiaro ExchangeEngine come engine così lo posso usare
	ExchangeEngine engine = new ExchangeEngine();
	
	// Cosa fa quando attivato
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Exchanges between users, activated!");
	}
	
	// Cosa fa quando disattivato
	@Override
	public void onDisable() {
		getLogger().info("Exchanges between users, deactivated!");
	}
	
	// Fa cose al primo login di un nuovo utente
	/*@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) throws Throwable {
	  if(!event.getPlayer().hasPlayedBefore()) {

	    }
	}*/
	
	// Dichiarazione dei comandi
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// Ti chiederai perché usare una stringa per l'UUID, beh, se il server è crackato
		// usa il nome di un utente al posto di un UUID dato da Mojang.
		String pUUID = null;
		
		// Controllo iniziale prima del comando
		// I comandi dunque funzionano solo se eseguiti da utenti.
	    if (!(sender instanceof Player))
	    {
		    sender.sendMessage("You must be a player!");
		    return false;
	    }
	    
	    // Se il server è originale, usa UUID, altrimenti usa nome del player.
	    if ((getServer().getOnlineMode())==true)
	    {
	    	pUUID = (((Player) sender).getUniqueId()).toString();
	    } else {
	    	pUUID = sender.getName();
	    }
	    	
	    // Comando /deposit | Mostra l'indirizzo sul quale l'utente deve depositare
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
		
		// Comando /balance | Mostra il saldo di un utente
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
	    
		// Comando /contract | Crea e accetta contratti
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
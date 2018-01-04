// This enables reddcoin exchanges in-game
// Author: Simone Cervino, @soxasora
package net.blockstreet.coin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.util.UUID;
//import org.bukkit.event.player.PlayerJoinEvent;
public class Exchange extends JavaPlugin implements Listener {
	
	// Dichiaro _JSON usando Gson
	final Gson _JSON;
	public Exchange() {
		_JSON = new GsonBuilder().setPrettyPrinting().create();
	}
	
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
	
	// Le api fondamentali per interfacciarsi a Reddconomy.
	@SuppressWarnings("rawtypes")
	private Map apiCall(String action) throws Exception
	{
		  String urlString = "http://localhost:8099/?action=" + action;
		  URL url = new URL(urlString);
		  HttpURLConnection con = (HttpURLConnection) url.openConnection();
		  
		  BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		  String output;
		  StringBuffer response = new StringBuffer();
		 
		  while ((output = in.readLine()) != null) {
		   response.append(output);
		  }
		  in.close();
		  
		  Map resp=_JSON.fromJson(response.toString(),Map.class);
		  return resp;
	}
	
	// Ottiene l'indirizzo per il deposito
	private String getAddrDeposit(long balance, String pUUID) throws Exception
	{
		 
		 String action = "deposit&wallid=" + pUUID + "&balance=" + balance;
		 Map data=(Map)apiCall(action).get("data");
		 return (String)data.get("addr");
	}
	
	// Crea contratto
	private String createContract(long ammount, String pUUID) throws Exception
	{
		String action = "newcontract&wallid=" + pUUID + "&ammount=" + ammount;
		Map data=(Map)apiCall(action).get("data");
		return (String)data.get("contractId");
	}
	 
	// Accetta contratto
	private void acceptContract(String contractId, String pUUID) throws Exception
	{
		apiCall("acceptcontract&wallid=" + pUUID + "&contractid=" + contractId);
	}
	 
	// Ottiene saldo di un utente
	private double getBalance(String pUUID) throws Exception
	{
		String action = "balance&wallid=" + pUUID;
		Map data=(Map)apiCall(action).get("data");
		Number balance=(Number)data.get("balance");
		return (balance.longValue())/100000000.0;
	}
	 
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
		
		// Comando /balance | Mostra il saldo di un utente
		else if (cmd.getName().equalsIgnoreCase("balance"))
	    {		    	
		    if (args.length==0)
		    	{
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
	    
		// Comando /contract | Crea e accetta contratti
		else if (cmd.getName().equalsIgnoreCase("contract"))
	    {		    	
			if (args.length==2)
		    	{
		    		if (args[0].equalsIgnoreCase("new"))
		    		{
		    			long ammount = (long)(Double.parseDouble(args[1])*100000000L);
		    			try {
							sender.sendMessage("Contract ID: " + createContract(ammount, pUUID));
							sender.sendMessage("Contract created. Share the Contract ID.");
						} catch (Exception e) {
							sender.sendMessage("Cannot create contract. Call an admin for more info.");
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		} else if (args[0].equalsIgnoreCase("accept")) {
		    			String contractId = args[1];
		    			try {
		    				acceptContract(contractId, pUUID);
							sender.sendMessage("Contract accepted.");
							sender.sendMessage("You now have: " + getBalance(pUUID) + " RDD");
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
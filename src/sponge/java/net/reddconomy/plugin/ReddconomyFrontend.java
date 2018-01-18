/*
 * Copyright (c) 2018, Simone Cervino.
 * 
 * This file is part of Reddconomy-sponge.

    Reddconomy-sponge is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Reddconomy-sponge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Reddconomy-sponge.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.reddconomy.plugin;
import net.reddconomy.plugin.ReddconomyApi;
import com.google.inject.Inject;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.block.tileentity.carrier.Dropper;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "reddconomy-sponge", name = "Reddconomy-sponge", version = "0.1")

public class ReddconomyFrontend implements CommandListener{
	
	// Declaring fundamental functions
	private String apiQR;
	private String apiCoin;
	private String pluginCSigns;
	private String apiUrl;
	private String secret;
	private boolean testmode;
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();
	ReddconomyApi api;
	
	@Inject
	Game game;
	
	@Inject
	Logger logger;
	
	// CONFIG BLOCK
	private ConfigurationNode config = null;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public File getDefaultConfig() {
        return this.defaultConfig;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
        return this.configManager;
    }
    
    // Declaring default configuration and loading configuration's settings.
	@Listener
	public void onPreInit(GamePreInitializationEvent event) throws IOException
	{
		try {

            if (!getDefaultConfig().exists()) {

                getDefaultConfig().createNewFile();
                this.config = getConfigManager().load();

                this.config.getNode("ConfigVersion").setValue(3);

                this.config.getNode("url").setValue("http://127.0.0.1:8099");
                this.config.getNode("qr").setValue("enabled");
                this.config.getNode("coin").setValue("reddcoin");
                this.config.getNode("csigns").setValue("enabled");
                this.config.getNode("testmode").setValue("true");
                this.config.getNode("secretkey").setValue("");
                getConfigManager().save(this.config);
                logger.log(Level.INFO, "Created default configuration, Reddxchange will not run until you have edited this file!");
                //TODO a control on the default configuration
            }

            this.config = getConfigManager().load();

        } catch (IOException exception) {

        	logger.log(Level.SEVERE,"Couldn't create default configuration file!");

        }
		apiUrl = this.config.getNode("url").getString();
	    apiQR = this.config.getNode("qr").getString();
	    apiCoin = this.config.getNode("coin").getString();
	    pluginCSigns = this.config.getNode("csigns").getString();
	    testmode = this.config.getNode("testmode").getBoolean();
	    secret = this.config.getNode("secretkey").getString();
	    api = new ReddconomyApi(apiUrl, secret);
		int version = this.config.getNode("ConfigVersion").getInt();
		logger.log(Level.INFO, "Configfile version is " + version + ".");
		
	}
	
	// Useful function to check if a player is an Operator.
	public boolean isOp(Player player)
	{
		if (player.hasPermission("Everything.everything"))
		return true;
		else return false;
	}
	
	

	// Register the Reddconomy command
    @Listener
    public void onInit(GameInitializationEvent event) {
    	logger.log(Level.INFO, "Reddxchange activated.");
    	CommandSpec cmds = CommandSpec.builder()
    			.description(Text.of("Command"))
    			.executor(new CommandHandler(this))
    			.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("args"))))
    			.build();
    	game.getCommandManager().register(this, cmds, "$","reddconomy","rdd");
    	

    }
    
    // Checking if the deposit has successfully happened.
    @Listener
    public void hasStarted(GameStartedServerEvent event) {
		Task.builder().execute(() -> processPendingDeposits())
	    .async().delay(1, TimeUnit.SECONDS).interval(10, TimeUnit.SECONDS)
	    .name("Fetch deposit status").submit(this);
		
		System.out.println(apiUrl);
    }
    
    private void processPendingDeposits() {
		final Iterator<String> it=_PENDING_DEPOSITS.iterator();

		while(it.hasNext()){
			String addr=it.next();
			try{
				final PendingDepositData deposit_data=api.getDepositStatus(addr);
				if(deposit_data.status!=1){
					it.remove();

					if(!deposit_data.addr.equals("[SRV]")){ // FIXME: Shouldn't be hardcoded
						Task.builder().execute((new Runnable(){
							public void run() {
								final UUID pUUID=UUID.fromString(deposit_data.addr);
								(Sponge.getServer().getPlayer(pUUID)).get()
								.sendMessage(Text.of(deposit_data.status==0?
										"Deposit completed. Check your balance!":"Deposit expired! Request another one."));
							}
						})).delay(0,TimeUnit.MILLISECONDS).name("Fetch deposit status").submit(this);
					}

				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
    
    // Give 1000 TEST coins to newbies.
    @Listener
    public void onPlayerFirstJoin(ClientConnectionEvent.Join event) throws Exception
    {
    		Player player = event.getTargetEntity();
    		if (!(player.get(JoinData.class).isPresent()))
    		{
	    		UUID USER_ADDR = player.getUniqueId();
	    		player.sendMessage(Text.of("Welcome to the Reddconomy Public Test Environment!"));
	    		player.sendMessage(Text.of("We're going to give you 1000 TEST-RDDs so you can test the server."));
	    		double money = -1000.0;
	    		long amount = (long)(money*100000000L);
	    		String cId = api.createServerContract(amount);
	    		api.acceptContract(cId, USER_ADDR);
	    		player.sendMessage(Text.of("Wait please. We're processing the transaction."));
	    		player.sendMessage(Text.of("Done! Check your balance with /balance"));
    		}
    }
    
    // Forcing player name in order to avoid robbing
    @Listener
    public void onSignPlace (ChangeSignEvent event)
    {
    		Task.builder().execute(()-> {
	    		TileEntity tile = event.getTargetTile();
		    	Player player = (Player) event.getSource();
		    	if (Utils.getLine(tile,0).equals("[CONTRACT]")){
		    		Utils.setLine(tile, 3, Text.of(player.getName()));	
		    	}
    		})
    		.delay(5, TimeUnit.MILLISECONDS)
    		.name("Forcing player's name in the contract sign").submit(this);
    }
    
    // Disabling dispenser/dropper access to other players than the owner.
    @Listener (order=Order.FIRST)
    public void onContainerAdjacentInteract (InteractBlockEvent.Secondary event)
    {
    	Player player = (Player) event.getSource();
	    	if (event.getTargetBlock().getLocation().isPresent())
	    	{
	    		Location<World> location = event.getTargetBlock().getLocation().get();
	    		if (location.getTileEntity().isPresent())
	    		{
	    			TileEntity tile = location.getTileEntity().get();
	    			if (tile instanceof Dispenser || tile instanceof Dropper)
	    			{
		    			if (!Utils.canPlayerOpen(location, player.getName()))
		    			{
		    				player.sendMessage(Text.of("[CONTRACT] Only the owner can open this container."));
		    				event.setCancelled(true);
		    			}
	    			}
	    		}
	    	}
    }
    
    // CONTRACT SIGNS BLOCK
    @Listener
    public void onSignInteract (InteractBlockEvent.Secondary event)
    {	    	
	    	if (event.getTargetBlock().getLocation().isPresent())
	    	{
	    		Location<World> location = event.getTargetBlock().getLocation().get();
	    		if (location.getTileEntity().isPresent())
	    		{
	    			TileEntity tile = location.getTileEntity().get();
	    			if (tile instanceof Sign)
	    			{
	    				// Getting the original position of the block
	    				Direction origdirection = location.get(Keys.DIRECTION).get();
	    		    	Player player = (Player) event.getSource();
	    		        if (pluginCSigns.equalsIgnoreCase("enabled"))
	    		        {
		    		        UUID pUUID = player.getUniqueId();
		    				String line0=Utils.getLine(tile,0);
		    				String line1=Utils.getLine(tile,1);
		    				String line2=Utils.getLine(tile,2);
		    				String line3=Utils.getLine(tile,3);
		    				BlockType origsign = location.getBlockType();
		    				if (line0.equals("[CONTRACT]"))
		    				{
		    					Player seller = Sponge.getServer().getPlayer(line3).get();
		    					UUID sellerUUID = seller.getUniqueId();
		    					
		    				  	long ammount = (long)(Double.parseDouble(line1)*100000000L);
		    					try {
		    						if (player != seller || testmode==true)
		    						{
			    						String cID = api.createContract(ammount, sellerUUID);
			    						int status = api.acceptContract(cID, pUUID);
			    						// This activates the redstone only if the contract replied with 200
			    						if (status==200)
			    						{
				    						player.sendMessage(Text.of("Contract ID: "+cID));
				    						player.sendMessage(Text.of("Contract accepted."));				    					
				    						location.setBlockType(BlockTypes.REDSTONE_TORCH);
				    						Task.builder().execute(()-> {
					    						BlockState state = origsign.getDefaultState();
					    						BlockState newstate = state.with(Keys.DIRECTION, origdirection).get();
					    						location.setBlock(newstate);
					    						TileEntity tile2 = location.getTileEntity().get();
					    						Utils.setLine(tile2, 0, Text.of(line0));
					    						Utils.setLine(tile2, 1, Text.of(line1));
					    						Utils.setLine(tile2, 2, Text.of(line2));
					    						Utils.setLine(tile2, 3, Text.of(line3));
				    						}) .delay(5, TimeUnit.MILLISECONDS)	 .name("Powering off Redstone.").submit(this);
				
			    						} else {
			    							player.sendMessage(Text.of("Check your balance. Cannot accept contract"));
			    						}
		    						} else {
		    							player.sendMessage(Text.of("You can't accept your own contract."));
		    						}
		    							 
		    					} catch (Exception e) {
		    						player.sendMessage(Text.of("Cannot create/accept contract. Call an admin for more info."));
		    						e.printStackTrace();
		    					}
		    				}
		    				
	    			} else if (pluginCSigns.equalsIgnoreCase("deactivated")) player.sendMessage(Text.of("Contract Signs aren't enabled. Sorry about that.")); 
	    		}
	    	}
        } 
    }

    // All the commands of Reddconomy
	@Override
	public boolean onCommand(CommandSource src, String command, String[] args) {
		if (!(src instanceof Player))return true;
		Player player = (Player) src;
		UUID pUUID = player.getUniqueId();
		boolean is_admin = isOp(player);
		try{	
			boolean invalid=false;
			switch (command) {
				// deposit
				case "deposit": {
					if(args.length<1){
						invalid=true;
						break;
					}
					double damount = Double.parseDouble(args[0]);
					long amount = (long) (damount * 100000000L);
					String addr = api.getAddrDeposit(amount, pUUID);
					if (addr!=null)
					{
						if (apiQR.equalsIgnoreCase("enabled")) {
							player.sendMessage(Text.of(api.createQR(addr, apiCoin, damount)));
						} else if (apiQR.equalsIgnoreCase("link")) {
							player.sendMessage(Text.of("TO BE IMPLEMENTED")); // TODO QR links
						}
			
						player.sendMessage(Text.of("Deposit " + damount + " " + apiCoin + " to this address: " + addr));
						_PENDING_DEPOSITS.add(addr);
					} else player.sendMessage(Text.of("Cannot create deposit address right now. Contact an admin."));
					break;
				}
				// balance
				case "balance": {
					double balance = api.getBalance(pUUID);
					if (balance!=-1)
					player.sendMessage(Text.of("You have: " + balance + " " + apiCoin));
					else player.sendMessage(Text.of("Cannot request balance right now. Contact an admin."));
					break;
				}
				// commands for OPs: send
				case "admin": {
					if (is_admin) {
						String action = args[0];
						if(args.length<1){
							invalid=true;
							break;
						}
						switch (action) {
							case "send": {
								if(args.length<3){
									invalid=true;
									break;
								}
								double text = Double.parseDouble(args[1]);
								String addr = args[2];
								long amount = (long) (text * 100000000L);
								int status = api.sendCoins(addr, amount);
								if (status==200)
								player.sendMessage(Text.of("Sending " + text + " to the address: " + addr));			
								else player.sendMessage(Text.of("Cannot request coins right now. Check the error in console"));
								break;
							}
							case "srvdeposit": {
								if (args.length<1) {
									invalid=true;
									break;
								}
								double damount = Double.parseDouble(args[1]);
								long amount = (long)(damount*100000000L);
								String addr = api.srvDeposit(amount);
								if (addr!=null)
								{
									if (apiQR.equalsIgnoreCase("enabled")) {
										player.sendMessage(Text.of(api.createQR(addr, apiCoin, damount)));
									} else if (apiQR.equalsIgnoreCase("link")) {
										player.sendMessage(Text.of("TO BE IMPLEMENTED")); // TODO QR links
									}
						
									player.sendMessage(Text.of("Deposit " + damount + " " + apiCoin + " to this address: " + addr));
									_PENDING_DEPOSITS.add(addr);
								} else player.sendMessage(Text.of("Cannot create deposit address right now. Check the server console."));
								break;
								}
							}
						} else player.sendMessage(Text.of("Forbidden for non-op"));
					break;
				}
				// withdraw
				case "withdraw": {
					if(args.length<2){
						invalid=true;
						break;
					}
					double text = Double.parseDouble(args[0]);
					String addr = args[1];
					long amount = (long) (text * 100000000L);
					int status = api.withdraw(amount, addr, pUUID);
					if (status==200)
					{
						player.sendMessage(Text.of("Withdrawing.. Wait at least 10 minutes"));	
					} else player.sendMessage(Text.of("Cannot request a withdraw right now, contact an admin."));
					break;
				}
				// contract
				case "contract": {
					if(args.length<2){
						invalid=true;
						break;
					}
					String method = args[0];
					String text = args[1];
					if (method.equals("new")) {
						long amount = (long) (Double.parseDouble(text) * 100000000L);
						String cId = api.createContract(amount, pUUID);
						if (cId!=null)
						player.sendMessage(Text.of("Share this Contract ID: " + api.createContract(amount, pUUID)));	
						else player.sendMessage(Text.of("Can't create contract right now. Contact an admin."));
					} else if (method.equals("accept")) {
						String contractId = text;
						int status = api.acceptContract(contractId, pUUID);
						if (status == 200) {
							player.sendMessage(Text.of("Contract accepted."));
							player.sendMessage(Text.of("You now have: " + api.getBalance(pUUID) + " RDD"));
						} else {
							player.sendMessage(Text.of("Cannot accept contract"));
						}
		
					}
					break;
				}
				default:
				// help/info or no args
				case "help":{
					sendHelpText(player);
					break;
				}
			}
			
			if(invalid){
				player.sendMessage(Text.of("Invalid Command"));
				sendHelpText(player);
			}
		}catch(Exception e){
			e.printStackTrace();
			player.sendMessage(Text.of("Unexpected error"));
		}
		return true;
	}

	// Help message of Reddconomy
	private void sendHelpText(Player player) {

		// TODO finish the help message
		player.sendMessage(Text.of("REDDCONOMY HELP"));
		player.sendMessage(Text.of("=====[COMMANDS]====="));
		player.sendMessage(Text.of("/$: shows info"));
		player.sendMessage(Text.of("/$ help: shows this"));
		player.sendMessage(Text.of("/$ deposit <amount>: Get the deposit address."));
		player.sendMessage(Text.of("/$ balance: Shows your balance."));
		player.sendMessage(Text.of("/$ withdraw <amount> <addr>: Withdraw money."));
		player.sendMessage(Text.of("/$ contract new <amount>: Create contract. (- sign for giving, no sign for requesting)"));
		player.sendMessage(Text.of("/$ contract accept <contractid>: Accept a contract."));
		player.sendMessage(Text.of("====[CONTRACT SIGNS]===="));
		player.sendMessage(Text.of("In order to make Contract Signs, you have to write in a sign:"));
		player.sendMessage(Text.of("FIRST LINE: [CONTRACT] | SECOND LINE: <amount>"));
		player.sendMessage(Text.of("Copyright (c) 2018, Riccardo Balbo, Simone Cervino. This plugin and all its components are released under GNU GPL v3 and BSD-2-Clause license."));
		player.sendMessage(Text.of("See ???? for more info."));	
	}
}
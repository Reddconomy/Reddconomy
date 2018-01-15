/*
 * @author Soxasora, Riccardo B.
 */
package net.reddconomy.plugin;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

import net.reddconomy.plugin.ReddconomyApi;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.block.tileentity.carrier.Dropper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "reddxchange", name = "Reddxchange", version = "0.0.1")

public class ReddconomyFrontend implements CommandListener{
	
	
	private String apiQR;
	private String apiCoin;
	private String pluginCSigns;
	private String apiUrl;
	private boolean testmode;
	ReddconomyApi api;
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();
	
	
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
    
	@Inject
	Game game;
	
	@Inject
	Logger logger;
	
	@Listener
	public void onPreInit(GamePreInitializationEvent event) throws IOException
	{
		try {

            if (!getDefaultConfig().exists()) {

                getDefaultConfig().createNewFile();
                this.config = getConfigManager().load();

                this.config.getNode("ConfigVersion").setValue(1);

                this.config.getNode("url").setValue("http://reddconomy.frk.wf:8099");
                this.config.getNode("qr").setValue("enabled");
                this.config.getNode("coin").setValue("reddcoin");
                this.config.getNode("csigns").setValue("enabled");
                this.config.getNode("testmode").setValue("true");
                getConfigManager().save(this.config);
                logger.log(Level.INFO, "Created default configuration, Reddxchange will not run until you have edited this file!");

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
	    api = new ReddconomyApi(apiUrl);
		int version = this.config.getNode("ConfigVersion").getInt();
		logger.log(Level.INFO, "Configfile version is " + version + ".");
		
	}
	
	
	

	
    @Listener
    public void onInit(GameInitializationEvent event) {
    	logger.log(Level.INFO, "Reddxchange activated.");
    	CommandSpec depositCmd = CommandSpec.builder()
    			.description(Text.of("Deposit command"))
    			.arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("amount"))))
    			.executor(new CommandHandler("depositCmd",this))
    			.build();
    	game.getCommandManager().register(this, depositCmd, "deposit");
    	CommandSpec balanceCmd = CommandSpec.builder()
    			.description(Text.of("Show balance"))
    			.arguments(GenericArguments.none())
    			.executor(new CommandHandler("balanceCmd",this))
    			.build();
    	game.getCommandManager().register(this, balanceCmd, "balance");
    	//TODO abilitare questo comando solo per testmode
    	if (testmode)
    	{
	    	CommandSpec sendcoinsCmd = CommandSpec.builder()
	    			.description(Text.of("Send coins to addr"))
	    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("addr")), GenericArguments.doubleNum(Text.of("amount"))))
	    			.executor(new CommandHandler("sendcoins",this))
	    			.build();
	    	game.getCommandManager().register(this, sendcoinsCmd, "sendcoins");
    	}
    	CommandSpec withdrawCmd = CommandSpec.builder()
    			.description(Text.of("Withdraw money"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("addr")), GenericArguments.doubleNum(Text.of("amount"))))
    			.executor(new CommandHandler("withdrawCmd",this))
    			.build();
    	game.getCommandManager().register(this, withdrawCmd, "withdraw");
    	CommandSpec contractCmd = CommandSpec.builder()
    			.description(Text.of("Create contract"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("method")), GenericArguments.string(Text.of("cIDorAmount"))))
    			.executor(new CommandHandler("contractCmd",this))
    			.build();
    	game.getCommandManager().register(this, contractCmd, "contract");
    	

    }
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
				final UUID pUUID=UUID.fromString(deposit_data.addr);
				if(deposit_data.status!=1){
					it.remove();
					Task.builder().execute((new Runnable() {
						public void run() {
							(Sponge.getServer().getPlayer(pUUID)).get().sendMessage(Text.of(
									deposit_data.status==0?"Deposit completed. Check your balance!":"Deposit expired! Request another one."
							));
						}		
					}))
						    .delay(0, TimeUnit.MILLISECONDS)
						    .name("Fetch deposit status").submit(this);
				}		
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
    
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
	    		player.sendMessage(Text.of("Doing some magic."));
	    		player.sendMessage(Text.of("."));
	    		player.sendMessage(Text.of("."));
	    		player.sendMessage(Text.of("Done! Check your balance with /balance"));
    		}
    }
    
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
    		.name("Powering off Redstone.").submit(this);
    }
    
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
		    						player.sendMessage(Text.of("This container is protected by The Contract Signs Law. Only the owner can open it."));
		    						event.setCancelled(true);
		    					}
	    			}
	    		}
	    	}
    }
    
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
    //TODO please retrieve data from sign for contracts it should be immutable something
    
    @Override
	public CommandResult onCommand(CommandSource src,String command, CommandContext args) {
		Player player = (Player) src;
		UUID pUUID = player.getUniqueId();
		if(src instanceof Player)
		if (command.equals("depositCmd"))
		{
				// TODO QR Deposits
				double text = ((Double) args.getOne("amount").get());
				long amount = (long)(text*100000000L);
				try {
					String addr=api.getAddrDeposit(amount, pUUID);
					if(apiQR.equalsIgnoreCase("enabled"))
					{
						player.sendMessage(Text.of(api.createQR(addr, apiCoin, args.getOne("amount").get().toString())));
						player.sendMessage(Text.of("Deposit "+text+" "+apiCoin+" to this address: "+addr));
					}
					else if(apiQR.equalsIgnoreCase("link"))
					{
						player.sendMessage(Text.of("NOT READY")); //TODO QR Links
						player.sendMessage(Text.of("Deposit "+text+" "+apiCoin+" to this address: "+addr));
					}
					else
					player.sendMessage(Text.of("Deposit "+text+" "+apiCoin+" to this address: "+addr));
					
					_PENDING_DEPOSITS.add(addr);
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		} else if (command.equals("balanceCmd"))
		{
			try {
				player.sendMessage(Text.of("You have: " + api.getBalance(pUUID) + " " +apiCoin));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (command.equals("sendcoins"))
		{
			double text = ((Double) args.getOne("amount").get());
			String addr = args.getOne("addr").get().toString();
			long amount = (long)(text*100000000L);
			try {
				api.sendCoins(addr, amount);
				player.sendMessage(Text.of("It worked! Now please wait for the confirmation."));
				player.sendMessage(Text.of("We're adding " + text + " to the address: " + addr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (command.equals("withdraw"))
		{
			double text = ((Double) args.getOne("amount").get());
			String addr = args.getOne("addr").get().toString();
			long amount = (long)(text*100000000L);
			try {
				api.withdraw(amount, addr, pUUID);
				player.sendMessage(Text.of("Withdrawing.."));
				player.sendMessage(Text.of("Ok, it should work, wait please."));
			} catch (Exception e) {
				player.sendMessage(Text.of("Something went wrong. Call an admin."));
			}
		} else if (command.equals("contract"))
		{
			String method = args.getOne("method").toString();
			String text = args.getOne("cIDorAmount").get().toString();
			if (method.equals("new"))
			{
				long amount = (long)(Double.parseDouble(text)*100000000L);
				try {
					player.sendMessage(Text.of("Share this Contract ID: " + api.createContract(amount, pUUID)));
				} catch (Exception e) {
					player.sendMessage(Text.of("Cannot create contract. Call an admin for more info."));
					e.printStackTrace();
				}
			} else if (method.equals("accept"))
			{
				String contractId = text;
				try {
					api.acceptContract(contractId, pUUID);
					player.sendMessage(Text.of("Contract accepted."));
					player.sendMessage(Text.of("You now have: " + api.getBalance(pUUID) + " RDD"));
				} catch (Exception e) {
					player.sendMessage(Text.of("Cannot accept contract. Are you sure that you haven't already accepted?"));
					player.sendMessage(Text.of("Otherwise, call and admin for more info."));
					e.printStackTrace();
				}
			}
		}
		else src.sendMessage(Text.of("Only a player can execute this!"));
		return CommandResult.success();
	}
}
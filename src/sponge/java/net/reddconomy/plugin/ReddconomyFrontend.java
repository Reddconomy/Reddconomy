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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import reddconomy.data.Info;
import reddconomy.offchain.fees.Fee;

@Plugin(id="reddconomy-sponge", name="Reddconomy-sponge", version="0.1.1")

public class ReddconomyFrontend implements CommandListener{

	// Declaring fundamental functions
	private String API_QR;
	private String PLUGIN_CONTRACT_SIGNS;
	private boolean CAN_RUN=true;
	private boolean DEBUG;
	// CONFIG BLOCK
	private ConfigurationNode CONFIG=null;
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();

	@Inject
	Game game;

	@Inject
	Logger logger;

	
	@Inject
	@DefaultConfig(sharedRoot=true)
	private File defaultConfig;

	@Inject
	@DefaultConfig(sharedRoot=true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;

	public File getDefaultConfig() {
		return this.defaultConfig;
	}

	public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
		return this.configManager;
	}

	// Declaring default configuration and loading configuration's settings.
	@Listener
	public void onPreInit(GamePreInitializationEvent event) throws Exception {
		try{

			if(!getDefaultConfig().exists()){

				getDefaultConfig().createNewFile();
				this.CONFIG=getConfigManager().load();

				this.CONFIG.getNode("ConfigVersion").setValue(5);

				this.CONFIG.getNode("url").setValue("http://changeme:8099");
				this.CONFIG.getNode("qr").setValue("enabled");
				this.CONFIG.getNode("csigns").setValue("enabled");
				this.CONFIG.getNode("debug").setValue("false");
				this.CONFIG.getNode("secretkey").setValue("");
				this.CONFIG.getNode("ifnewgift").setValue("false");
				this.CONFIG.getNode("gift").setValue("1000.0");
				getConfigManager().save(this.CONFIG);
				logger.log(Level.INFO,"Created default configuration, Reddxchange will not run until you have edited this file!");
			}

			this.CONFIG=getConfigManager().load();

		}catch(IOException exception){

			logger.log(Level.SEVERE,"Couldn't create default configuration file!");

		}
		
		ReddconomyApi.init(this.CONFIG.getNode("url").getString(),this.CONFIG.getNode("secretkey").getString());
		Task.builder().execute(ReddconomyApi::updateInfo).async().interval(60,TimeUnit.SECONDS).name("Update backend info").submit(this);

		API_QR=this.CONFIG.getNode("qr").getString();
		PLUGIN_CONTRACT_SIGNS=this.CONFIG.getNode("csigns").getString();
		DEBUG=this.CONFIG.getNode("debug").getBoolean();
		int version=this.CONFIG.getNode("ConfigVersion").getInt();
		logger.log(Level.INFO,"Configfile version is "+version+".");
		if(this.CONFIG.getNode("url").getString().equals("http://changeme:8099")){
			logger.log(Level.SEVERE,"Reddconomy-sponge will not start until you modify the generated config.");
			CAN_RUN=false;
		}else{
			CAN_RUN=true;
		}
	}

	// Useful function to check if a player is an Operator.
	public boolean isOp(Player player) {
		if(player.hasPermission("Everything.everything")) return true;
		else return false;
	}

	// Register the Reddconomy command
	@Listener
	public void onInit(GameInitializationEvent event) {
		if(CAN_RUN){
			logger.log(Level.INFO,"Reddconomy-sponge is now activated.");
			CommandSpec cmds=CommandSpec.builder().description(Text.of("Command")).executor(new CommandHandler(this)).arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("args")))).build();
			game.getCommandManager().register(this,cmds,"$","reddconomy","rdd");
		}else logger.log(Level.SEVERE,"Reddconomy-sponge is deactivated, modify the config.");
	}

	// Checking if the deposit has successfully happened.
	@Listener
	public void hasStarted(GameStartedServerEvent event) {
		Task.builder().execute(() -> processPendingDeposits()).async().delay(1,TimeUnit.SECONDS).interval(10,TimeUnit.SECONDS).name("Fetch deposit status").submit(this);
	}

	private void processPendingDeposits() {
		final Iterator<String> it=_PENDING_DEPOSITS.iterator();

		while(it.hasNext()){
			String addr=it.next();
			try{
				final PendingDepositData deposit_data=ReddconomyApi.getDepositStatus(addr);
				if(deposit_data.status!=1){
					it.remove();

					if(!deposit_data.addr.equals("[SRV]")){ // FIXME: Shouldn't be hardcoded
						Task.builder().execute((new Runnable(){
							public void run() {
								final UUID pUUID=UUID.fromString(deposit_data.addr);
								(Sponge.getServer().getPlayer(pUUID)).get().sendMessage(Text.of(deposit_data.status==0?"Deposit completed. Check your balance!":"Deposit expired! Request another one."));
							}
						})).delay(0,TimeUnit.MILLISECONDS).name("Fetch deposit status").submit(this);
					}

				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	// CURRENTLY DISABLED DUE TO SPONGEAPI BUG
	// Bug description: firstPlayed and lastPlayed slightly differs breaking the hasPlayedBefore method.
	// Give configurable TEST coins to newbies.
	/*@Listener
	public void onPlayerFirstJoin(ClientConnectionEvent.Join event) throws Exception
	{
			System.out.println("Welcome bonus status: "+ifnewgift);
			if (ifnewgift)
			{
				Player player = event.getTargetEntity();
				System.out.println(player.firstPlayed());
				System.out.println(player.lastPlayed());
				if (player.firstPlayed().equals(player.lastPlayed()))
				{
					System.out.println("They do equal.");
				} else System.out.println("They don't equal");
				System.out.println("well at least it loads the config");
	    		
	    		if (!player.hasPlayedBefore())
	    		{
		    		UUID USER_ADDR = player.getUniqueId();
		    		player.sendMessage(Text.of("Welcome to the Reddconomy Public Test Environment!"));
		    		player.sendMessage(Text.of("We're going to give you 1000 TEST-RDDs so you can test the server."));
		    		long amount = (long)(-gift*100000000L);
		    		String cId = api.createServerContract(amount);
		    		api.acceptContract(cId, USER_ADDR);
		    		player.sendMessage(Text.of("Wait please. We're processing the transaction."));
		    		player.sendMessage(Text.of("Done! Check your balance with /balance"));
	    		} else if (player.hasPlayedBefore()) System.out.println("This player isn't new or the method isn't working");
			}
	}*/

	// Forcing player name in order to avoid robbing
	@Listener
	public void onSignPlace(ChangeSignEvent event) {
		Task.builder().execute(() -> {
			TileEntity tile=event.getTargetTile();
			Player player=(Player)event.getSource();
			if(Utils.getLine(tile,0).equals("[CONTRACT]")){
				if(!isOp(player)) Utils.setLine(tile,3,Text.of(player.getName()));
			}
		}).delay(5,TimeUnit.MILLISECONDS).name("Forcing player name in the contract sign").submit(this);
	}

	// Disabling dispenser/dropper access to other players than the owner.
	@Listener(order=Order.FIRST)
	public void onContainerAdjacentInteract(InteractBlockEvent.Secondary event) {
		Player player=(Player)event.getSource();
		if(event.getTargetBlock().getLocation().isPresent()){
			Location<World> location=event.getTargetBlock().getLocation().get();
			if(location.getTileEntity().isPresent()){
				TileEntity tile=location.getTileEntity().get();
				if(tile instanceof Dispenser||tile instanceof Dropper){
					if(!Utils.canPlayerOpen(location,player.getName())&&!isOp(player)){

						player.sendMessage(Text.of(TextColors.DARK_RED,"[CONTRACT] Only the owner can open this container."));
						if(isOp(player)) player.sendMessage(Text.of("You're op btw."));
						event.setCancelled(true);
					}
				}
			}
		}
	}

	// CONTRACT SIGNS BLOCK
	@Listener
	public void onSignInteract(InteractBlockEvent.Secondary event) {
		if(event.getTargetBlock().getLocation().isPresent()){
			Location<World> location=event.getTargetBlock().getLocation().get();
			if(location.getTileEntity().isPresent()){
				TileEntity tile=location.getTileEntity().get();
				if(tile instanceof Sign){
					// Getting the original position of the block
					Direction origdirection=location.get(Keys.DIRECTION).get();
					Player player=(Player)event.getSource();
					if(PLUGIN_CONTRACT_SIGNS.equalsIgnoreCase("enabled")){
						UUID pUUID=player.getUniqueId();
						String line0=Utils.getLine(tile,0);
						String line1=Utils.getLine(tile,1);
						String line2=Utils.getLine(tile,2);
						String line3=Utils.getLine(tile,3);
						BlockType origsign=location.getBlockType();
						if(line0.equals("[CONTRACT]")){
							Player seller=Sponge.getServer().getPlayer(line3).get();
							UUID sellerUUID=seller.getUniqueId();

							long ammount=(long)(Double.parseDouble(line1)*100000000L);
							try{
								if(player!=seller||DEBUG){
									String cID=ReddconomyApi.createContract(ammount,sellerUUID);
									int status=ReddconomyApi.acceptContract(cID,pUUID);
									// This activates the redstone only if the contract replied with 200
									if(status==200){
										player.sendMessage(Text.of("Contract ID: "+cID));
										player.sendMessage(Text.of("Contract accepted."));
										location.setBlockType(BlockTypes.REDSTONE_TORCH);
										Task.builder().execute(() -> {
											BlockState state=origsign.getDefaultState();
											BlockState newstate=state.with(Keys.DIRECTION,origdirection).get();
											location.setBlock(newstate);
											TileEntity tile2=location.getTileEntity().get();
											Utils.setLine(tile2,0,Text.of(line0));
											Utils.setLine(tile2,1,Text.of(line1));
											Utils.setLine(tile2,2,Text.of(line2));
											Utils.setLine(tile2,3,Text.of(line3));
										}).delay(5,TimeUnit.MILLISECONDS).name("Powering off Redstone.").submit(this);

									}else{
										player.sendMessage(Text.of(TextColors.DARK_RED,"Check your balance. Cannot accept contract"));
									}
								}else{
									player.sendMessage(Text.of(TextColors.DARK_RED,"You can't accept your own contract."));
								}

							}catch(Exception e){
								player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot create/accept contract. Call an admin for more info."));
								e.printStackTrace();
							}
						}

					}else if(PLUGIN_CONTRACT_SIGNS.equalsIgnoreCase("deactivated")) player.sendMessage(Text.of(TextColors.BLUE,"Contract Signs aren't enabled. Sorry about that."));
				}
			}
		}
	}

	// All the commands of Reddconomy
	@Override
	public boolean onCommand(CommandSource src, String command, String[] args) {
		if(!(src instanceof Player)) return true;
		Player player=(Player)src;
		UUID pUUID=player.getUniqueId();
		boolean is_admin=isOp(player);
		try{
			boolean invalid=false;
			switch(command){
				// deposit
				case "deposit":{
					if(args.length<1){
						invalid=true;
						break;
					}
					double damount=Double.parseDouble(args[0]);
					long amount=(long)(damount*100000000L);
					String addr=ReddconomyApi.getAddrDeposit(amount,pUUID);
					if(addr!=null){
						if(API_QR.equalsIgnoreCase("enabled")){
							player.sendMessage(Text.of(ReddconomyApi.createQR(addr,ReddconomyApi.getInfo().coin,damount)));
						}else if(API_QR.equalsIgnoreCase("link")){
							player.sendMessage(Text.of(TextColors.GOLD,"TO BE IMPLEMENTED")); // TODO QR links
						}

						player.sendMessage(Text.of("Deposit "+damount+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short+" to this address: "+addr));
						_PENDING_DEPOSITS.add(addr);
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot create deposit address right now. Contact an admin."));
					break;
				}
				// balance
				case "balance":{
					double balance=ReddconomyApi.getBalance(pUUID);
					if(balance!=-1) player.sendMessage(Text.of("You have: "+balance+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short));
					else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request balance right now. Contact an admin."));
					break;
				}
				case "info":{
					showInfos(player);
					break;
				}
				// commands for OPs: send
				case "admin":{
					if(is_admin){
						String action=args[0];
						if(args.length<1){
							invalid=true;
							break;
						}
						switch(action){
							case "help":{
								sendAdminHelpText(player);
								break;
							}
							case "info":{
								showAdminInfos(player);
								break;
							}
							case "send":{
								if(args.length<3){
									invalid=true;
									break;
								}
								double text=Double.parseDouble(args[1]);
								String addr=args[2];
								long amount=(long)(text*100000000L);
								int status=ReddconomyApi.sendCoins(addr,amount);
								if(status==200) player.sendMessage(Text.of("Sending "+text+" to the address: "+addr));
								else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request coins right now. Check the error in console"));
								break;
							}
							case "deposit":{
								if(args.length<3){
									invalid=true;
									break;
								}
								String wallid=args[2];
								double damount=Double.parseDouble(args[1]);
								long amount=(long)(damount*100000000L);
								String addr=ReddconomyApi.getAddrDeposit(amount,wallid);
								if(addr!=null){
									if(API_QR.equalsIgnoreCase("enabled")) player.sendMessage(Text.of(ReddconomyApi.createQR(addr,ReddconomyApi.getInfo().coin,damount)));
									else if(API_QR.equalsIgnoreCase("link")) player.sendMessage(Text.of("TO BE IMPLEMENTED")); // TODO QR links
									player.sendMessage(Text.of("Deposit "+damount+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short+" to this address: "+addr));
								}else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot create deposit address right now. Check the server console."));
								break;
							}
							case "balance":{
								if(args.length<2){
									invalid=true;
									break;
								}
								String wallid=args[1];
								double balance=ReddconomyApi.getBalance(wallid);
								if(balance!=-1) player.sendMessage(Text.of("You have: "+balance+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short));
								else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request balance right now. Contact an admin."));
								break;
							}
							case "withdraw":{
								if(args.length<4){
									invalid=true;
									break;
								}
								String wallid=args[2];
								String addr=args[3];
								double damount=Double.parseDouble(args[1]);
								long amount=(long)(damount*100000000L);
								int status=ReddconomyApi.withdraw(amount,addr,wallid);
								if(status==200)	player.sendMessage(Text.of(TextColors.BLUE,"Withdrawing.. Wait at least 10 minutes"));
								else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request a withdraw right now, check the Reddconomy Service error."));
								break;
							}
						}
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Forbidden for non-op"));
					break;
				}
				// withdraw
				case "withdraw":{
					if(args.length<2){
						invalid=true;
						break;
					}
					double damount=Double.parseDouble(args[0]);
					String addr=args[1];
					long amount=(long)(damount*100000000L);
					int status=ReddconomyApi.withdraw(amount,addr,pUUID);
					if(status==200){
						player.sendMessage(Text.of(TextColors.BLUE,"Withdrawing "+damount+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short+".. Wait at least 10 minutes"));
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request a withdraw right now, contact an admin."));
					break;
				}
				// tip
				case "tipsrv":
				case "tip":{
					boolean server=command.equals("tipsrv");
					if(args.length<2&&(!server||args.length<1)){
						invalid=true;
						break;
					}

					double damount=Double.parseDouble(args[0]);
					long amount=(long)(damount*100000000L);
					String to;
					Player receiver=null;
					if(server){
						to=ReddconomyApi.getInfo().generic_wallid;						
					}else{
						receiver=(Player)Sponge.getServer().getPlayer(args[1]).get();
						to=receiver.getUniqueId().toString();
					}
					String cId=ReddconomyApi.createContract(-amount,pUUID);
					if(cId!=null){
						int status=ReddconomyApi.acceptContract(cId,to);
						if(status==200){
							player.sendMessage(Text.of(TextColors.GOLD,
									damount+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")+ReddconomyApi.getInfo().coin_short
									+" sent to the "+(server?"server":"user "+args[1])));
							if(receiver!=null)	receiver.sendMessage(Text.of(TextColors.GOLD,player.getName()
									+" sent you a tip worth "+damount+" "+(ReddconomyApi.getInfo().testnet?"testnet ":" ")
									+ReddconomyApi.getInfo().coin_short+"!"));
						}else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot send tip, "
								+ "check your balance or contact an admin."));
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Something went wrong, contact an admin."));
				}
				// contract
				case "contract":{
					if(args.length<2){
						invalid=true;
						break;
					}
					String method=args[0];
					String text=args[1];
					if(method.equals("new")){
						long amount=(long)(Double.parseDouble(text)*100000000L);
						String cId=ReddconomyApi.createContract(amount,pUUID);
						if(cId!=null) player.sendMessage(Text.of("Share this Contract ID: "+ReddconomyApi.createContract(amount,pUUID)));
						else player.sendMessage(Text.of(TextColors.DARK_RED,"Can't create contract right now. Contact an admin."));
					}else if(method.equals("accept")){
						String contractId=text;
						int status=ReddconomyApi.acceptContract(contractId,pUUID);
						if(status==200){
							player.sendMessage(Text.of(TextColors.GOLD,"Contract accepted."));
							player.sendMessage(Text.of("You now have: "+ReddconomyApi.getBalance(pUUID)+" RDD"));
						}else{
							player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot accept contract. Are you sure that you haven't already accepted?"));
							player.sendMessage(Text.of(TextColors.GOLD,"Otherwise, call and admin for more info."));
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
				player.sendMessage(Text.of(TextColors.DARK_RED,"Invalid Command"));
				sendHelpText(player);
			}
		}catch(Exception e){
			e.printStackTrace();
			player.sendMessage(Text.of(TextColors.DARK_RED,"Unexpected error"));
		}
		return true;
	}

	private void showInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.GOLD,"On testnet? ",TextColors.WHITE,ReddconomyApi.getInfo().testnet));
		player.sendMessage(Text.of(TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin));
		player.sendMessage(Text.of(TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()));
	}

	private void showAdminInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.GOLD,"On testnet? ",TextColors.WHITE,ReddconomyApi.getInfo().testnet));
		player.sendMessage(Text.of(TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin));
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Tip: ",TextColors.WHITE,reddconomy.Utils.convertToUserFriendly(ReddconomyApi.getInfo().welcome_tip)));
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Tip wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().welcome_funds_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Generic Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().generic_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Fee Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().fees_collector_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()));
	}

	// Help message of Reddconomy
	private void sendHelpText(Player player) throws MalformedURLException {

		URL github=new URL("https://github.com/Reddconomy");
		Text moreinfo=Text.builder("Click here for more info!").color(TextColors.GOLD).onClick(TextActions.openUrl(github)).build();
		player.sendMessage(Text.of(TextColors.BLUE,"REDDCONOMY HELP"));
		player.sendMessage(Text.of(TextColors.BLUE,"=====[COMMANDS]====="));
		player.sendMessage(Text.of(TextColors.GOLD,"/$",": shows info"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ help",": shows this"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ deposit <amount>",": Get the deposit address."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ balance",": Shows your balance."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ withdraw <amount> <addr>",": Withdraw money."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ contract new <amount>",": Create contract. (- sign for giving, no sign for requesting)"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ contract accept <contractid>",": Accept a contract."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ tip <amount> <user>",": Tip an user."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ tipsrv <amount>",": Tip the server."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ info",": Get info from backend."));
		player.sendMessage(Text.of(TextColors.BLUE,"===[CONTRACT SIGNS]==="));
		player.sendMessage(Text.of("In order to make Contract Signs, you have to write in a sign:"));
		player.sendMessage(Text.of(TextColors.GOLD,"FIRST LINE:"," [CONTRACT] | ",TextColors.GOLD,"SECOND LINE: ","<amount>"));
		player.sendMessage(Text.of(TextColors.BLUE,"========[INFOs]========"));
		player.sendMessage(Text.of("Copyright (c) 2018, Riccardo Balbo, Simone Cervino. This plugin and all its components are released under GNU GPL v3 and BSD-2-Clause license."));
		player.sendMessage(moreinfo);
	}

	// Admin help message of Reddconomy
	private void sendAdminHelpText(Player player) {
		player.sendMessage(Text.of(TextColors.BLUE,"REDDCONOMY HELP"));
		player.sendMessage(Text.of(TextColors.BLUE,"=====[COMMANDS]====="));
		player.sendMessage(Text.of(TextColors.BLUE,"/$ admin help",TextColors.WHITE,": Shows this"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin send <amount> <addr>",TextColors.WHITE,": Send coins from the backend."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin deposit <amount> <wallid>",TextColors.WHITE,": Deposit into wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin balance <wallid>",TextColors.WHITE,": Get balance of wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin withdraw <amount> <wallid> <addr>",TextColors.WHITE,": Withdraw from wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin info",TextColors.WHITE,": Shows info and status about the backend."));
	}
}
package net.reddconomy.plugin;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;


import com.google.inject.Inject;

import net.reddconomy.plugin.ReddconomyApi;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

@Plugin(id = "reddxchange", name = "Reddxchange", version = "0.0.1")

public class ReddconomyFrontend implements CommandListener{
	
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();
    String reddconomy_api_url = "http://reddconomy.frk.wf:8099";
    ReddconomyApi api = new ReddconomyApi(reddconomy_api_url);
	
	@Inject
	Game game;
	
	@Inject
	Logger logger;
	
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
    	CommandSpec sendcoinsCmd = CommandSpec.builder()
    			.description(Text.of("Send coins to addr"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("addr")), GenericArguments.doubleNum(Text.of("amount"))))
    			.executor(new CommandHandler("sendcoins",this))
    			.build();
    	game.getCommandManager().register(this, sendcoinsCmd, "sendcoins");
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
					player.sendMessage(Text.of("Deposit "+text+/*+coin+*/" RDD to this address: "+addr));
					_PENDING_DEPOSITS.add(addr);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		} else if (command.equals("balanceCmd"))
		{
			try {
				player.sendMessage(Text.of("You have: " + api.getBalance(pUUID) + " RDD"));
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
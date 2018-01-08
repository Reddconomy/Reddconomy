package net.reddconomy.plugin.sponge;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;


import com.google.inject.Inject;

import net.reddconomy.plugin.sponge.ReddconomyApi_sponge;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

@Plugin(id = "reddxchange", name = "Reddxchange", version = "0.0.1")
public class ReddconomyFrontend_sponge {
	
	private final ConcurrentLinkedQueue<String> _PENDING_DEPOSITS=new ConcurrentLinkedQueue<String>();
	
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
    			.executor(new ReddExecutor("depositCmd"))
    			.build();
    	game.getCommandManager().register(this, depositCmd, "deposit");
    	CommandSpec balanceCmd = CommandSpec.builder()
    			.description(Text.of("Show balance"))
    			.arguments(GenericArguments.none())
    			.executor(new ReddExecutor("balanceCmd"))
    			.build();
    	game.getCommandManager().register(this, balanceCmd, "balance");
    	//TODO abilitare questo comando solo per testmode
    	CommandSpec sendcoinsCmd = CommandSpec.builder()
    			.description(Text.of("Send coins to addr"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("addr")), GenericArguments.doubleNum(Text.of("amount"))))
    			.executor(new ReddExecutor("sendcoins"))
    			.build();
    	game.getCommandManager().register(this, sendcoinsCmd, "sendcoins");
    	CommandSpec withdrawCmd = CommandSpec.builder()
    			.description(Text.of("Withdraw money"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("addr")), GenericArguments.doubleNum(Text.of("amount"))))
    			.executor(new ReddExecutor("withdrawCmd"))
    			.build();
    	game.getCommandManager().register(this, withdrawCmd, "withdraw");
    	CommandSpec contractCmd = CommandSpec.builder()
    			.description(Text.of("Create contract"))
    			.arguments(GenericArguments.seq(GenericArguments.string(Text.of("method")), GenericArguments.string(Text.of("cIDorAmount"))))
    			.executor(new ReddExecutor("contractCmd"))
    			.build();
    	game.getCommandManager().register(this, contractCmd, "contract");
    }
    
    String reddconomy_api_url = "http://reddconomy.frk.wf:8099";
    ReddconomyApi_sponge api = new ReddconomyApi_sponge(reddconomy_api_url);
    
    @Listener
    public void onServerStart (GameStartedServerEvent event)
    {
		Task task = Task.builder().execute(() -> processPendingDeposits())
		    .async().delay(500, TimeUnit.MILLISECONDS).interval(30, TimeUnit.SECONDS)
		    .name("Fetch deposit status").submit(this);
    }
    
    private void processPendingDeposits() {
		final Iterator<String> it=_PENDING_DEPOSITS.iterator();

		while(it.hasNext()){
			String addr=it.next();
		
			try{
				final PendingDepositData_sponge deposit_data=api.getDepositStatus(addr);
				final UUID pUUID=UUID.fromString(deposit_data.addr);
				if(deposit_data.status!=1){
					it.remove();
					Task task = Task.builder().execute((new Runnable() {
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
}
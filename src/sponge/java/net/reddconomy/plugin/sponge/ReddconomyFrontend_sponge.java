package net.reddconomy.plugin.sponge;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;


import com.google.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

@Plugin(id = "reddxchange", name = "Reddxchange", version = "0.0.1")
public class ReddconomyFrontend_sponge {
	
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
    
    
}
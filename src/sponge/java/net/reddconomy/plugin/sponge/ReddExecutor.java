package net.reddconomy.plugin.sponge;

import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import net.reddconomy.plugin.spigot.ReddconomyApi_spigot;

public class ReddExecutor implements CommandExecutor {
	
	String reddconomy_api_url = "https://reddconomy.frk.wf:8099";
	ReddconomyApi_spigot api = new ReddconomyApi_spigot(reddconomy_api_url);
	final String _COMMAND;
	public ReddExecutor (String commandChosen)
	{
		_COMMAND = commandChosen;
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Player player = (Player) src;
		UUID pUUID = player.getUniqueId();
		if(src instanceof Player)
		if (_COMMAND.equals("depositCmd"))
		{
				// TODO QR Deposits
				double text = ((Double) args.getOne("amount").get());
				long amount = (long)(text*100000000L);
				try {
					String addr=api.getAddrDeposit(amount, pUUID);
					player.sendMessage(Text.of("Deposit "+text+/*+coin+*/" RDD to this address: "+addr));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		} else if (_COMMAND.equals("balanceCmd"))
		{
			try {
				player.sendMessage(Text.of("You have: " + api.getBalance(pUUID) + " RDD"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (_COMMAND.equals("sendcoins"))
		{
			double text = ((Double) args.getOne("amount").get());
			String addr = args.getOne("addr").toString();
			long amount = (long)(text*100000000L);
			try {
				api.sendCoins(addr, amount);
				player.sendMessage(Text.of("It worked!"));
				player.sendMessage(Text.of("You added " + text + " to the address: " + addr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (_COMMAND.equals("withdraw"))
		{
			double text = ((Double) args.getOne("amount").get());
			String addr = args.getOne("addr").toString();
			long amount = (long)(text*100000000L);
			try {
				api.withdraw(amount, addr, pUUID);
				player.sendMessage(Text.of("Withdrawing.."));
				player.sendMessage(Text.of("Ok, it should work, wait please."));
			} catch (Exception e) {
				player.sendMessage(Text.of("Something went wrong. Call an admin."));
			}
		} else if (_COMMAND.equals("contract"))
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

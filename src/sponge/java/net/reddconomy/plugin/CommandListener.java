package net.reddconomy.plugin;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public interface CommandListener {
	public CommandResult onCommand(CommandSource src,String command, CommandContext args);
}
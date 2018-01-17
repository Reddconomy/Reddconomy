package net.reddconomy.plugin;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public interface CommandListener {
	public boolean onCommand(CommandSource src,String command, String[] args);
}
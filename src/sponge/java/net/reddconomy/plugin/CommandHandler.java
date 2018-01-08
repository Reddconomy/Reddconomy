package net.reddconomy.plugin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import net.reddconomy.plugin.CommandListener;

public class CommandHandler  implements CommandExecutor {
	private final String _COMMAND;
	private final CommandListener _LISTENER;
	public CommandHandler(String command,CommandListener listener){
		_LISTENER=listener;
		_COMMAND=command;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return _LISTENER.onCommand(src,_COMMAND,args);
	}
}
package net.reddconomy.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import com.google.inject.Inject;

import net.reddconomy.plugin.CommandListener;

public class CommandHandler  implements CommandExecutor {


	private final CommandListener _LISTENER;
	public CommandHandler(CommandListener listener){
		_LISTENER=listener;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext fargs) throws CommandException {
		
		
		String[] args= fargs.getOne("args").orElse("").toString().split(" ");
		
		String cmd="info";
		if(args.length>0){
			
			cmd= args[0];
			String argss[] = new String[args.length-1];
			System.arraycopy(args, 1, argss, 0, argss.length);
			args = argss;
		}
    	System.out.println("Execute command "+cmd+" "+Arrays.deepToString(args));
		boolean ret = _LISTENER.onCommand(src, cmd, args);
		return ret ? CommandResult.success() : CommandResult.empty();
	}
}
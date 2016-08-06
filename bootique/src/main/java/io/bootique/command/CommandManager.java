package io.bootique.command;

import java.util.Collection;

/**
 * A service interface that provides the rest of Bootique with access to
 * available commands.
 * 
 * @since 0.12
 */
public interface CommandManager {

	Command getCommand(String name);

	/**
	 * Returns all available commands excluding default command.
	 * 
	 * @return all available commands excluding default command.
	 */
	Collection<Command> getCommands();
	
	Command getDefaultCommand();
}

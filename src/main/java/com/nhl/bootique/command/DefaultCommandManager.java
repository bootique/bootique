package com.nhl.bootique.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.nhl.bootique.annotation.DefaultCommand;

/**
 * @since 0.12
 */
public class DefaultCommandManager implements CommandManager {

	private Map<String, Command> activeCommands;
	private Command defaultCommand;

	@Inject
	public DefaultCommandManager(Set<Command> allCommands, @DefaultCommand Command defaultCommand) {

		this.defaultCommand = defaultCommand;
		this.activeCommands = new HashMap<>();

		allCommands.forEach(c -> {

			String name = c.getMetadata().getName();
			Command existing = activeCommands.put(name, c);

			// complain on dupes
			if (existing != null && existing != c) {
				String c1 = existing.getClass().getName();
				String c2 = c.getClass().getName();
				throw new RuntimeException(
						String.format("Duplicate command for name %s (provided by: %s and %s) ", name, c1, c2));
			}
		});
	}

	@Override
	public Command getCommand(String name) {
		return activeCommands.getOrDefault(name, defaultCommand);
	}

	@Override
	public Collection<Command> getCommands() {
		return activeCommands.values();
	}
	
	@Override
	public Command getDefaultCommand() {
		return defaultCommand;
	}
}

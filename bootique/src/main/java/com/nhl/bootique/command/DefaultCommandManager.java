package com.nhl.bootique.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since 0.12
 */
public class DefaultCommandManager implements CommandManager {

	private Map<String, Command> activeCommands;
	private Command defaultCommand;

	public static DefaultCommandManager create(Set<Command> commands, Command defaultCommand) {
		Map<String, Command> commandMap = new HashMap<>();

		commands.forEach(c -> {

			String name = c.getMetadata().getName();
			Command existing = commandMap.put(name, c);

			// complain on dupes
			if (existing != null && existing != c) {
				String c1 = existing.getClass().getName();
				String c2 = c.getClass().getName();
				throw new RuntimeException(
						String.format("Duplicate command for name %s (provided by: %s and %s) ", name, c1, c2));
			}
		});

		DefaultCommandManager commandManager = new DefaultCommandManager();
		commandManager.defaultCommand = defaultCommand;
		commandManager.activeCommands = commandMap;
		return commandManager;
	}

	public static DefaultCommandManager create(Map<String, Command> commands, Command defaultCommand) {
		DefaultCommandManager commandManager = new DefaultCommandManager();
		commandManager.defaultCommand = defaultCommand;
		commandManager.activeCommands = commands;
		return commandManager;
	}

	private DefaultCommandManager() {
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

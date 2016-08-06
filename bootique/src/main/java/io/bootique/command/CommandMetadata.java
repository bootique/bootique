package io.bootique.command;

import java.util.ArrayList;
import java.util.Collection;

import io.bootique.cli.CliOption;

/**
 * Describes a {@link Command}, providing useful information to Bootique to map
 * command to command-line parameters and generate help.
 * 
 * @since 0.12
 */
public class CommandMetadata {

	public static Builder builder(Class<? extends Command> commandType) {
		return new Builder().commandType(commandType);
	}

	public static Builder builder(String commandName) {
		return new Builder().name(commandName);
	}

	private String name;
	private Collection<CliOption> options;
	private String description;

	public CommandMetadata() {
		this.options = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public Collection<CliOption> getOptions() {
		return options;
	}

	public String getDescription() {
		return description;
	}

	public static class Builder {

		private CommandMetadata metadata;

		private Builder() {
			this.metadata = new CommandMetadata();
		}

		public CommandMetadata build() {
			return metadata;
		}

		public Builder commandType(Class<? extends Command> commandType) {
			metadata.name = defaultName(commandType);
			return this;
		}

		public Builder name(String name) {
			metadata.name = name;
			return this;
		}

		public Builder description(String description) {
			this.metadata.description = description;
			return this;
		}

		public Builder addOption(CliOption option) {
			this.metadata.options.add(option);
			return this;
		}

		public Builder addOption(CliOption.Builder optionBuilder) {
			return addOption(optionBuilder.build());
		}

		// TODO: copy/paste from ConfigModule... reuse somehow...
		private String defaultName(Class<? extends Command> commandType) {
			String name = commandType.getSimpleName().toLowerCase();
			final String stripSuffix = "command";
			return (name.endsWith(stripSuffix)) ? name.substring(0, name.length() - stripSuffix.length()) : name;
		}
	}
}

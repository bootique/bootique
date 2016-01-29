package com.nhl.bootique.command;

/**
 * @since 0.12
 */
public class CommandOption {

	public static Builder builder(String name) {
		return new Builder().name(name);
	}

	public static Builder builder(String name, String description) {
		return new Builder().name(name).description(description);
	}

	private String name;
	private CommandOptionValueCardinality valueCardinality;
	private String description;
	private String valueDescription;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public CommandOptionValueCardinality getValueCardinality() {
		return valueCardinality;
	}

	public String getValueDescription() {
		return valueDescription;
	}

	public static class Builder {

		private CommandOption option;

		private Builder() {
			this.option = new CommandOption();
			this.option.valueCardinality = CommandOptionValueCardinality.NONE;
		}

		public Builder name(String name) {
			this.option.name = name;
			return this;
		}

		public Builder description(String description) {
			this.option.description = description;
			return this;
		}

		public Builder valueRequired(String valueDescription) {
			this.option.valueCardinality = CommandOptionValueCardinality.REQUIRED;
			this.option.valueDescription = valueDescription;
			return this;
		}

		public Builder valueOptional(String valueDescription) {
			this.option.valueCardinality = CommandOptionValueCardinality.OPTIONAL;
			this.option.valueDescription = valueDescription;
			return this;
		}

		public CommandOption build() {
			return option;
		}
	}

}

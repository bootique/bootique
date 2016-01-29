package com.nhl.bootique.cli;

/**
 * A descriptor of a command-line option.
 * 
 * @since 0.12
 */
public class CliOption {

	public static Builder builder(String name) {
		return new Builder().name(name);
	}

	public static Builder builder(String name, String description) {
		return new Builder().name(name).description(description);
	}

	private String name;
	private CliOptionValueCardinality valueCardinality;
	private String description;
	private String valueDescription;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public CliOptionValueCardinality getValueCardinality() {
		return valueCardinality;
	}

	public String getValueDescription() {
		return valueDescription;
	}

	public static class Builder {

		private CliOption option;

		private Builder() {
			this.option = new CliOption();
			this.option.valueCardinality = CliOptionValueCardinality.NONE;
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
			this.option.valueCardinality = CliOptionValueCardinality.REQUIRED;
			this.option.valueDescription = valueDescription;
			return this;
		}

		public Builder valueOptional(String valueDescription) {
			this.option.valueCardinality = CliOptionValueCardinality.OPTIONAL;
			this.option.valueDescription = valueDescription;
			return this;
		}

		public CliOption build() {
			return option;
		}
	}

}

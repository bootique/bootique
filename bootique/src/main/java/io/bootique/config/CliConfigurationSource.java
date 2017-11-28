package io.bootique.config;

import io.bootique.cli.Cli;
import io.bootique.log.BootLogger;
import io.bootique.resource.ResourceFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link ConfigurationSource} that locates configuration in a resource
 * specified via command-line '--config' option.
 */
public class CliConfigurationSource implements ConfigurationSource {

    public static final String CONFIG_OPTION = "config";

    private List<String> locations;
    private BootLogger bootLogger;

    /**
     * @param cli
     * @param bootLogger
     * @deprecated since 0.25 in favor of builder API.
     */
    @Deprecated
    public CliConfigurationSource(Cli cli, BootLogger bootLogger) {
        this.locations = cli.optionStrings(CONFIG_OPTION);
        this.bootLogger = bootLogger;
    }

    protected CliConfigurationSource(List<String> locations, BootLogger bootLogger) {
        this.locations = locations;
        this.bootLogger = bootLogger;
    }

    public static Builder builder(BootLogger bootLogger) {
        return new Builder(bootLogger);
    }

    @Override
    public Stream<URL> get() {
        return locations.stream().map(this::toURL);
    }

    protected URL toURL(String location) {
        bootLogger.trace(() -> "Reading configuration at " + location);
        return new ResourceFactory(location).getUrl();
    }

    /**
     * @since 0.25
     */
    public static class Builder {
        private BootLogger bootLogger;
        private List<String> cliConfigs;
        private Set<String> diConfigs;

        protected Builder(BootLogger bootLogger) {
            this.bootLogger = bootLogger;
            this.cliConfigs = Collections.emptyList();
            this.diConfigs = Collections.emptySet();
        }

        public Builder cliConfigs(Cli cli) {
            this.cliConfigs = cli.optionStrings(CONFIG_OPTION);
            return this;
        }

        public Builder diConfigs(Set<String> diConfigs) {
            this.diConfigs = diConfigs;
            return this;
        }

        public CliConfigurationSource build() {
            return new CliConfigurationSource(mergeConfigs(), bootLogger);
        }

        private List<String> mergeConfigs() {

            // DI configs go first in undefined order; CLI configs follow in the order they were declared
            // on command line

            if (diConfigs.isEmpty()) {
                return cliConfigs;
            }

            List<String> merged = new ArrayList<>(diConfigs);
            merged.addAll(cliConfigs);
            return merged;
        }
    }
}

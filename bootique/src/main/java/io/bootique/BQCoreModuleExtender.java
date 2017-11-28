package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.annotation.DIConfigs;
import io.bootique.annotation.DefaultCommand;
import io.bootique.annotation.EnvironmentProperties;
import io.bootique.annotation.EnvironmentVariables;
import io.bootique.annotation.LogLevels;
import io.bootique.command.Command;
import io.bootique.command.CommandDecorator;
import io.bootique.command.CommandRefDecorated;
import io.bootique.env.DeclaredVariable;
import io.bootique.meta.application.OptionMetadata;

import java.util.Map;
import java.util.logging.Level;

import static java.util.Arrays.asList;

/**
 * Provides API to contribute custom extensions to BQCoreModule.  This class is a syntactic sugar for Guice
 * MapBinder and Multibinder.
 *
 * @since 0.22
 */
public class BQCoreModuleExtender extends ModuleExtender<BQCoreModuleExtender> {

    private Multibinder<String> configs;
    private MapBinder<String, String> properties;
    private MapBinder<String, String> variables;
    private MapBinder<String, Level> logLevels;
    private Multibinder<DeclaredVariable> declaredVariables;
    private Multibinder<OptionMetadata> options;
    private Multibinder<Command> commands;
    private Multibinder<CommandRefDecorated> commandDecorators;

    protected BQCoreModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public BQCoreModuleExtender initAllExtensions() {
        contributeConfigs();
        contributeProperties();
        contributeVariables();
        contributeVariableDeclarations();
        contributeLogLevels();
        contributeOptions();
        contributeCommands();
        contributeCommandDecorators();

        return this;
    }

    /**
     * Initializes optional default command that will be executed if no explicit command is found in startup arguments.
     *
     * @param commandType a class of the default command.
     * @return this extender instance.
     */
    public BQCoreModuleExtender setDefaultCommand(Class<? extends Command> commandType) {
        binder.bind(Key.get(Command.class, DefaultCommand.class)).to(commandType).in(Singleton.class);
        return this;
    }

    /**
     * Initializes optional default command that will be executed if no explicit command is found in startup arguments.
     *
     * @param command an instance of the default command.
     * @return this extender instance.
     */
    public BQCoreModuleExtender setDefaultCommand(Command command) {
        binder.bind(Key.get(Command.class, DefaultCommand.class)).toInstance(command);
        return this;
    }

    /**
     * Binds an optional application description used in help messages, etc.
     *
     * @param description optional application description used in help messages, etc.
     * @return this extender instance.
     */
    public BQCoreModuleExtender setApplicationDescription(String description) {
        binder.bind(ApplicationDescription.class).toInstance(new ApplicationDescription(description));
        return this;
    }

    public BQCoreModuleExtender setLogLevel(String name, Level level) {
        contributeLogLevels().addBinding(name).toInstance(level);
        return this;
    }

    public BQCoreModuleExtender setLogLevels(Map<String, Level> levels) {
        levels.forEach(this::setLogLevel);
        return this;
    }

    public BQCoreModuleExtender setProperty(String name, String value) {
        contributeProperties().addBinding(name).toInstance(value);
        return this;
    }

    public BQCoreModuleExtender setProperties(Map<String, String> properties) {
        properties.forEach(this::setProperty);
        return this;
    }

    public BQCoreModuleExtender setVar(String name, String value) {
        contributeVariables().addBinding(name).toInstance(value);
        return this;
    }

    public BQCoreModuleExtender setVars(Map<String, String> vars) {
        vars.forEach(this::setVar);
        return this;
    }

    /**
     * Declares a configuration variable for the given config path and given name.
     *
     * @param configPath a dot-separated "path" that navigates through the configuration tree to the property that
     *                   should be bound form a variable. E.g. "jdbc.myds.password".
     * @param name       public name of the variable.
     * @return this extender instance.
     */
    public BQCoreModuleExtender declareVar(String configPath, String name) {
        DeclaredVariable var = new DeclaredVariable(configPath, name);
        contributeVariableDeclarations().addBinding().toInstance(var);
        return this;
    }

    public BQCoreModuleExtender declareVars(Map<String, String> varsByConfigPaths) {
        varsByConfigPaths.forEach(this::declareVar);
        return this;
    }

    /**
     * Registers a URL of a configuration resource to be loaded by the app unconditionally and prior to any explicitly
     * specified configs. Can be called multiple times for multiple resources.
     *
     * @param configResourceId a resource path compatible with {@link io.bootique.resource.ResourceFactory} denoting
     *                         a configuration source. E.g. "a/b/my.yml", or "classpath:com/foo/another.yml".
     * @return this extender instance.
     * @since 0.25
     */
    public BQCoreModuleExtender addConfig(String configResourceId) {
        contributeConfigs().addBinding().toInstance(configResourceId);
        return this;
    }

    /**
     * Adds a new option to the list of Bootique CLI options.
     *
     * @param option a descriptor of the CLI option to be added to Bootique.
     * @return this extender instance.
     */
    public BQCoreModuleExtender addOption(OptionMetadata option) {
        contributeOptions().addBinding().toInstance(option);
        return this;
    }

    /**
     * Adds zero or more new options to the list of Bootique CLI options.
     *
     * @param options an array of descriptors of the CLI options to be added to Bootique.
     * @return this extender instance.
     */
    public BQCoreModuleExtender addOptions(OptionMetadata... options) {
        if (options != null) {
            asList(options).forEach(this::addOption);
        }
        return this;
    }

    /**
     * Associates the CLI option with a config path. The option runtime value is assigned to the configuration property
     * denoted by the path.
     *
     * @param configPath a dot-separated "path" that navigates configuration tree to the desired property. E.g.
     *                   "jdbc.myds.password".
     * @param name       alias of an option
     * @return this extender instance
     * @since 0.24
     */
    public BQCoreModuleExtender addOption(String configPath, String name) {
        contributeOptions().addBinding().toInstance(
                OptionMetadata.builder(name)
                        .configPath(configPath)
                        .valueRequired()
                        .build());
        return this;
    }

    /**
     * Associates the CLI option with a config path. The option runtime value is assigned to the configuration property
     * denoted by the path. Default value provided here will be used if the option is present, but no value is specified
     * on the command line.
     *
     * @param configPath   a dot-separated "path" that navigates configuration tree to the desired property. E.g.
     *                     "jdbc.myds.password".
     * @param defaultValue default option value
     * @param name         alias of an option
     * @return this extender instance
     * @since 0.24
     */
    public BQCoreModuleExtender addOption(String configPath, String defaultValue, String name) {
        contributeOptions().addBinding().toInstance(
                OptionMetadata.builder(name)
                        .configPath(configPath)
                        .defaultValue(defaultValue)
                        .valueOptional()
                        .build());
        return this;
    }

    /**
     * Associates the CLI option value with a config resource. This way a single option can be used to enable a complex
     * configuration.
     *
     * @param configResourceId a resource path compatible with {@link io.bootique.resource.ResourceFactory} denoting
     *                         a configuration source. E.g. "a/b/my.yml", or "classpath:com/foo/another.yml".
     * @param name             alias of an option
     * @return this extender instance
     * @since 0.24
     */
    public BQCoreModuleExtender addConfigResourceOption(String configResourceId, String name) {
        contributeOptions().addBinding().toInstance(
                OptionMetadata.builder(name)
                        .configResource(configResourceId)
                        .valueOptional()
                        .build());
        return this;
    }

    public BQCoreModuleExtender addCommand(Command command) {
        contributeCommands().addBinding().toInstance(command);
        return this;
    }

    public BQCoreModuleExtender addCommand(Class<? extends Command> commandType) {
        // TODO: what does singleton scope means when adding to collection?
        contributeCommands().addBinding().to(commandType).in(Singleton.class);
        return this;
    }

    /**
     * Decorates a given command. When that command is invoked, other commands defined in the decorator will be invoked
     * as well.
     *
     * @param commandType      "Raw" command type
     * @param commandDecorator command decorator.
     * @return this extender instance
     * @since 0.25
     */
    public BQCoreModuleExtender decorateCommand(Class<? extends Command> commandType, CommandDecorator commandDecorator) {
        contributeCommandDecorators().addBinding().toInstance(new CommandRefDecorated(commandType, commandDecorator));
        return this;
    }

    protected MapBinder<String, Level> contributeLogLevels() {
        return logLevels != null ? logLevels : (logLevels = newMap(String.class, Level.class, LogLevels.class));
    }

    protected Multibinder<OptionMetadata> contributeOptions() {
        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        return options != null ? options : (options = newSet(OptionMetadata.class));
    }

    protected Multibinder<DeclaredVariable> contributeVariableDeclarations() {
        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        return declaredVariables != null ? declaredVariables : (declaredVariables = newSet(DeclaredVariable.class));
    }

    protected Multibinder<Command> contributeCommands() {
        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        return commands != null ? commands : (commands = newSet(Command.class));
    }

    protected Multibinder<CommandRefDecorated> contributeCommandDecorators() {
        return commandDecorators != null ? commandDecorators : (commandDecorators = newSet(CommandRefDecorated.class));
    }

    protected MapBinder<String, String> contributeProperties() {
        return properties != null ? properties : (properties = newMap(String.class, String.class, EnvironmentProperties.class));
    }

    protected MapBinder<String, String> contributeVariables() {
        return variables != null ? variables : (variables = newMap(String.class, String.class, EnvironmentVariables.class));
    }

    protected Multibinder<String> contributeConfigs() {
        return configs != null ? configs : (configs = newSet(String.class, DIConfigs.class));
    }
}

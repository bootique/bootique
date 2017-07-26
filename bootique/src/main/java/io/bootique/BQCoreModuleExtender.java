package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.annotation.DefaultCommand;
import io.bootique.annotation.EnvironmentProperties;
import io.bootique.annotation.EnvironmentVariables;
import io.bootique.annotation.LogLevels;
import io.bootique.command.Command;
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

    private MapBinder<String, String> properties;
    private MapBinder<String, String> variables;
    private MapBinder<String, Level> logLevels;
    private Multibinder<DeclaredVariable> declaredVariables;
    private Multibinder<OptionMetadata> options;
    private Multibinder<Command> commands;

    protected BQCoreModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public BQCoreModuleExtender initAllExtensions() {
        contributeProperties();
        contributeVariables();
        contributeVariableDeclarations();
        contributeLogLevels();
        contributeOptions();
        contributeCommands();

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
     * Declares a configuration variable for the given config path. The variable will be included in the help
     * "ENVIRONMENT" section. The name of the variable will be derived from the config path.  E.g.
     * "jdbc.myds.password" becomes "BQ_JDBC_MYDS_PASSWORD".
     *
     * @param configPath a dot-separated "path" that navigates through the configuration tree to the property that
     *                   should be bound form a variable. E.g. "jdbc.myds.password".
     * @return this extender instance.
     */
    public BQCoreModuleExtender declareVar(String configPath) {
        new DeclaredVariableBinder(contributeVariableDeclarations(), configPath).withCanonicalName();
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
        new DeclaredVariableBinder(contributeVariableDeclarations(), configPath).withName(name);
        return this;
    }

    public BQCoreModuleExtender declareVars(Map<String, String> varsByConfigPaths) {
        varsByConfigPaths.forEach(this::declareVar);
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
     * Alias the CLI option value to a config path.
     *
     * @param configPath a dot-separated "path" that navigates through the configuration tree to the property that
     *                   should be bound from an option. E.g. "jdbc.myds.password".
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
     * Alias the CLI option value to a config path.
     *
     * @param configPath   a dot-separated "path" that navigates through the configuration tree to the property that
     *                     should be bound from an option. E.g. "jdbc.myds.password".
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

    public BQCoreModuleExtender addConfigFileOption(String configFilePath, String name) {
        contributeOptions().addBinding().toInstance(
                OptionMetadata.builder(name)
                        .configFilePath(configFilePath)
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

    protected MapBinder<String, String> contributeProperties() {
        return properties != null ? properties : (properties = newMap(String.class, String.class, EnvironmentProperties.class));
    }

    protected MapBinder<String, String> contributeVariables() {
        return variables != null ? variables : (variables = newMap(String.class, String.class, EnvironmentVariables.class));
    }
}

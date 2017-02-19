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
 * Provides API to contribute custom extensions to BQCoreModule.  This is a convenience API facade to Google Guice MapBinder and Multibinder.
 *
 * @since 0.22
 */
public class BQCoreModuleExtender {

    private Binder binder;

    private MapBinder<String, String> properties;
    private MapBinder<String, String> variables;
    private MapBinder<String, Level> logLevels;
    private Multibinder<DeclaredVariable> declaredVariables;
    private Multibinder<OptionMetadata> options;

    protected BQCoreModuleExtender(Binder binder) {
        this.binder = binder;
    }

    /**
     * Should be called by owning Module to initialize all contribution maps and collections. Failure to call this
     * method may result in injection failures for empty maps and collections.
     */
    BQCoreModuleExtender initAllExtensions() {
        getOrCreatePropertiesBinder();
        getOrCreateVariablesBinder();
        getOrCreateDeclaredVariablesBinder();
        getOrCreateLogLevelsBinder();
        getOrCreateOptionsBinder();

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
        getOrCreateLogLevelsBinder().addBinding(name).toInstance(level);
        return this;
    }

    public BQCoreModuleExtender setLogLevels(Map<String, Level> levels) {
        levels.forEach(this::setLogLevel);
        return this;
    }

    public BQCoreModuleExtender setProperty(String name, String value) {
        getOrCreatePropertiesBinder().addBinding(name).toInstance(value);
        return this;
    }

    public BQCoreModuleExtender setProperties(Map<String, String> properties) {
        properties.forEach(this::setProperty);
        return this;
    }

    public BQCoreModuleExtender setVar(String name, String value) {
        getOrCreateVariablesBinder().addBinding(name).toInstance(value);
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
        new DeclaredVariableBinder(getOrCreateDeclaredVariablesBinder(), configPath).withCanonicalName();
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
        new DeclaredVariableBinder(getOrCreateDeclaredVariablesBinder(), configPath).withName(name);
        return this;
    }

    public BQCoreModuleExtender declareVars(Map<String, String> varsByConfigPaths) {
        varsByConfigPaths.forEach(this::declareVar);
        return this;
    }

    public BQCoreModuleExtender setOption(OptionMetadata option) {
        getOrCreateOptionsBinder().addBinding().toInstance(option);
        return this;
    }

    public BQCoreModuleExtender setOptions(OptionMetadata... options) {
        if (options != null) {
            asList(options).forEach(this::setOption);
        }
        return this;
    }

    protected MapBinder<String, Level> getOrCreateLogLevelsBinder() {
        if (logLevels == null) {
            logLevels = MapBinder.newMapBinder(binder, String.class, Level.class, LogLevels.class);
        }

        return logLevels;
    }

    protected Multibinder<OptionMetadata> getOrCreateOptionsBinder() {

        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        if (options == null) {
            options = Multibinder.newSetBinder(binder, OptionMetadata.class);
        }

        return options;
    }

    protected Multibinder<DeclaredVariable> getOrCreateDeclaredVariablesBinder() {

        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        if (declaredVariables == null) {
            declaredVariables = Multibinder.newSetBinder(binder, DeclaredVariable.class);
        }

        return declaredVariables;
    }


    protected MapBinder<String, String> getOrCreatePropertiesBinder() {

        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        if (properties == null) {
            properties = MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
        }

        return properties;
    }

    protected MapBinder<String, String> getOrCreateVariablesBinder() {

        // no synchronization. we don't care if it is created twice. It will still work with Guice.
        if (variables == null) {
            variables = MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentVariables.class);
        }

        return variables;
    }
}

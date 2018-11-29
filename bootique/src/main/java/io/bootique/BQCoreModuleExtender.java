/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
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
import io.bootique.config.OptionRefWithConfig;
import io.bootique.config.OptionRefWithConfigPath;
import io.bootique.env.DeclaredVariable;
import io.bootique.help.ValueObjectDescriptor;
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
    private Multibinder<OptionRefWithConfig> optionDecorators;
    private MapBinder<Class<?>, ValueObjectDescriptor> valueObjectsDescriptors;
    private Multibinder<OptionRefWithConfigPath> optionPathDecorators;

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
        contributeOptionDecorators();
        contributeValueObjectsDescriptors();
        contributeOptionPathDecorators();

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
        return declareVar(configPath, name, null);
    }

    /**
     * @param configPath  a dot-separated "path" that navigates through the configuration tree to the property that
     *                    should be bound form a variable. E.g. "jdbc.myds.password".
     * @param name        public name of the variable.
     * @param description the description for variable.
     * @return this extender instance.
     * @since 1.0.RC1
     * Declares a configuration variable for the given config path, given name and given description.
     */
    public BQCoreModuleExtender declareVar(String configPath, String name, String description) {
        DeclaredVariable var = new DeclaredVariable(configPath, name, description);
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
     * Maps a CLI option to a URL of a configuration resource to be conditionally loaded by the app when
     * that option is used. The config is loaded prior to any configuration potentially loaded via the option.
     * This method can be called multiple times for the same option, mapping multiple config resources.
     * An option with "optionName" must be declared separately via {@link #addOption(OptionMetadata)} or
     * {@link #addOptions(OptionMetadata...)}.
     *
     * @param optionName       the name of the CLI option
     * @param configResourceId a resource path compatible with {@link io.bootique.resource.ResourceFactory} denoting
     *                         a configuration source. E.g. "a/b/my.yml", or "classpath:com/foo/another.yml".
     * @return this extender instance.
     * @since 0.25
     * @since 1.0.RC1 renamed from addConfigOnOption to mapConfigResource
     */
    public BQCoreModuleExtender mapConfigResource(String optionName, String configResourceId) {
        // using Multibinder to support multiple decorators for the same option
        contributeOptionDecorators().addBinding()
                .toInstance(new OptionRefWithConfig(optionName, configResourceId));
        return this;
    }

    /**
     * Maps a CLI option to a config path. The option runtime value is assigned to the
     * configuration property denoted by the path. An option with "optionName" must be declared separately via
     * {@link #addOption(OptionMetadata)} or {@link #addOptions(OptionMetadata...)}.
     *
     * @param optionName the name of the CLI option
     * @param configPath a dot-separated "path" that navigates configuration tree to the desired property.
     *                   E.g. "jdbc.myds.password".
     * @return this extender instance
     * @since 1.0.RC1
     */
    public BQCoreModuleExtender mapConfigPath(String optionName, String configPath) {
        contributeOptionPathDecorators().addBinding()
                .toInstance(new OptionRefWithConfigPath(optionName, configPath));
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

    /**
     * Binds valueObjectsDescriptors with string description to value objects.
     *
     * @param valueObjectsDescriptors - collection of value objects with valueObjectsDescriptors.
     * @return this extender instance
     * @since 1.0.RC1
     */
    public BQCoreModuleExtender addValueObjectsDescriptors(Map<Class<?>, ValueObjectDescriptor> valueObjectsDescriptors) {
        MapBinder<Class<?>, ValueObjectDescriptor> binder = contributeValueObjectsDescriptors();
        valueObjectsDescriptors.forEach((key, value) -> binder.addBinding(key).toInstance(value));
        return this;
    }

    /**
     * Binds descriptors with string description to value objects.
     *
     * @param object - the value object
     * @param valueObjectsDescriptor - descriptor for value object.
     * @return this extender instance
     * @since 1.0.RC1
     */
    public BQCoreModuleExtender addValueObjectDescriptor(Class<?> object, ValueObjectDescriptor valueObjectsDescriptor) {
         contributeValueObjectsDescriptors().addBinding(object).toInstance(valueObjectsDescriptor);
         return this;
    }

    protected MapBinder<Class<?>, ValueObjectDescriptor> contributeValueObjectsDescriptors() {
        return valueObjectsDescriptors != null
                ? valueObjectsDescriptors
                : (valueObjectsDescriptors = newMap(new TypeLiteral<Class<?>>() {}, TypeLiteral.get(ValueObjectDescriptor.class)));
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

    protected Multibinder<OptionRefWithConfig> contributeOptionDecorators() {
        return optionDecorators != null ? optionDecorators : (optionDecorators = newSet(OptionRefWithConfig.class));
    }

    protected Multibinder<OptionRefWithConfigPath> contributeOptionPathDecorators() {
        return optionPathDecorators != null ? optionPathDecorators : (optionPathDecorators = newSet(OptionRefWithConfigPath.class));
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

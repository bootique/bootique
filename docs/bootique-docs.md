---
title: "Bootique Core Documentation"
metaKeywords: "Bootique Framework Documentation version 0"
metaDescription: "Bootique: A Minimally Opinionated Framework for Runnable Java Apps - Documentation version 0"
---

## Part I. Overview

### Chapter 1. What is Bootique

Bootique is a minimally opinionated technology for building container-less runnable Java applications. No JavaEE container required to run your app! It is an ideal platform for [_microservices_](http://martinfowler.com/articles/microservices.html), as it allows you to create a fully functional app with minimal-to-no setup. Though it is not limited to a specific kind of app (or the "micro" size) and can be used for REST services, webapps, runnable jobs, DB migrations, JavaFX GUI apps to mention a few examples.

Unlike traditional container-based apps, Bootique allows you to control your `main()` method and create Java apps that behave like simple executable commands that can be run with Java:

```bash
java -jar my.jar [arguments]
```

Each Bootique app can be started with a YAML configuration loaded from a file or from a remote URL. Among other benefits, such configuration approach ideally suits cloud deployment environments.

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io/) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique favors modularity and clean pluggable architecture. Bootique is built on top of [Google Guice](https://github.com/google/guice) dependency injection (DI) container, which provides the core of its modularity mechanism. This means that pretty much anything in Bootique can be customized/overridden to your liking.

### Chapter 2. Java Version

Java 8 or newer is required.

### Chapter 3. Build System

Bootique apps can be built using any Java build system (Ant, Maven, Gradle, etc). Examples in the documentation are based on Maven and `maven-shade-plugin`. While this is not strictly a requirement, Bootique apps are usually packaged into "fat" runnable jars and don't have any external dependencies beyond the JRE.

### Chapter 4. Programming Skills

Everything you know about Java programming will be applicable when working with Bootique. You may need to "unlearn" some of the practices related to JavaEE configuration and container deployment though.

Integration between various parts of a Bootique app is done via [Google Guice](https://github.com/google/guice). In most cases Bootique API would steer you towards idiomatic approach to integration, so deep knowledge of Guice is not required. Though it wouldn't hurt to understand a few main concepts: modules, bindings, [multibindings](https://github.com/google/guice/wiki/Multibindings), [overrides](http://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/util/Modules.html).

Java [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) facility is another important part of Bootique, and probably yet another thing that you shouldn't worry too much about initially.

## Part II. Programming

### Chapter 5. Modules

Bootique apps are made of "modules". The framework simply locates all available modules, loads them in the DI environment, parses the command line, and then transfers control to a Command (that can originate from any of the modules) that matched the user choice. There's a growing list of modules created by Bootique development team. And you can easily write your own. In fact, programming in Bootique is primarily about writing Modules.

A module is a Java library that contains some code. What makes it a module is a special Java class that implements [Guice Module interface](https://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/Module.html). This class defines what "services" or other types of objects the module provides (in other words what will be injectable by the module users). This is done in a form of "bindings", i.e. associations between publicly visible injectable service interfaces and specific implementations:

```java
public class MyModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class);
    }
}
```

There are other flavors of bindings in Guice. Please refer to [Guice documentation](https://github.com/google/guice/wiki/Motivation) for details. One important form extensively used in Bootique is [Multibinding](https://github.com/google/guice/wiki/Multibindings).

### Chapter 6. Modules Auto-Loading

Modules can be automatically loaded via `Bootique.autoLoadModules()` as long as they are included in your aplication dependencies. Auto-loading depends on the Java [ServiceLoader mechanism](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html). To ensure your modules can be auto-loaded do two things. First implement `io.bootique.BQModuleProvider` interface specific to your module:

```java
public class MyModuleProvider implements BQModuleProvider {
    @Override
    public Module module() {
        return new MyModule();
    }
}
```

After that create a file `META-INF/services/io.bootique.BQModuleProvider` with the only line being the name of your BQModuleProvider implementor. E.g.:

```text
com.foo.MyModuleProvider
```

`BQModuleProvider` has two more methods that you can optionally implement to help Bootique to make sense of the module being loaded:

```java
public class MyModuleProvider implements BQModuleProvider {
    // ...
    
    // provides human-readable name of the module
    @Override
    public String name() {
        return "CustomName";
    }
    
    // a collection of modules whose services are overridden by this module
    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(BQCoreModule.class);
    }
}
```

If in your Module you are planning to redefine any services from the upstream modules, specify those upstream modules in the `overrides()` collection. In practice overrides are rarely needed, and often can be replaced with service decomposition.

### Chapter 7. Configuration and Configurable Factories

Bootique Modules obtain their configuration in a form of "factory objects". We'll show some examples shortly. For now let's focus on the big picture, namely the fact that Bootique app configuration is multi-layered and roughly follows the sequence of "code - config files (contributed) - config files (CLI) - overrides". "Code" is the default values that are provided in constructors of factory objects. Config files overlay those defaults with their own values. Config files can be either contributed in the code, or specified on the command line. Files is where the bulk of configuration usually stored. Finally config values may be further overridden via Java properties and/or environment variables.

#### Configuration via YAML Files

Format of configuration file can be either JSON or YAML. For simplicity we'll focus on YAML format, but the two are interchnageable. Here is an example config file:

```yaml
log:
  level: warn
  appenders:
    - type: file
      logFormat: '%c{20}: %m%n'
      file: target/logback/debug.log

jetty:
  context: /myapp
  connectors:
    - port: 12009
```
While not strictly required, as a rule the top-level keys in the file belong to configuration objects of individual modules. In the example above "log" subtree configures `bootique-logback` module, while "jetty" subtree configures `bootique-jetty`. For standard modules refer to module-specific documentation on the structure of the supported configuration (or run your app `-H` flag to print supported config to the console). Here we'll discuss how to build your own configuration-aware module.

Bootique allows each Module to read its specific configuration subree as an object of the type defined in the Module. Very often such an object is written as a factory that contains a bunch of setters for configuration properties, and a factory method to produce some "service" that a Module is interested in. Here is an example factory:

```java
public class MyFactory {

    private int intProperty;
    private String stringProperty;

    public void setIntProperty(int i) {
        this.intProperty = i;
    }

    public void setStringProperty(String s) {
        this.stringProperty = s;
    }

    // factory method
    public MyService createMyService(SomeOtherService soService) {
        return new MyServiceImpl(soService, intProperty, stringProperty);
    }
}
```

The factory contains configuration property declarations, as well as public setters for these properties (You can create getters as well. It is not strictly required, but may be useful for unit tests, etc.). Now let's take a look at the Module class:

```java
public class MyModule extends ConfigModule {
    @Provides
    public MyService createMyService(
             ConfigurationFactory configFactory, 
             SomeOtherService soService) {

        return configFactory
                 .config(MyFactory.class, configPrefix)
                 .createMySerice(soService);
    }
}
```

And now a sample configuration that will work with our module:

```yaml
my:
  intProperty: 55
  stringProperty: 'Hello, world!'
```

A few points to note here:

* Calling our module "MyModule" and extending from `ConfigModule` gives it access to the protected "configPrefix" instance variable that is initialized to the value of "my" (the naming convention here is to use the Module simple class name without the "Module" suffix and converted to lowercase).
* `@Provides` annotation is a Guice way of marking a Module method as a "provider" for a certain type of injectable service. All its parameters are themselves injectable objects.
* `ConfigurationFactory` is the class used to bind a subtree of the app YAML configuration to a given Java object (in our case - MyFactory). The structure of MyFactory is very simple here, but it can be as complex as needed, containing nested objects, arrays, maps, etc. Internally Bootique uses [Jackson framework](http://wiki.fasterxml.com/JacksonHome) to bind YAML to a Java class, so all the features of Jackson can be used to craft configuration.

#### Configuration File Loading

There are a number of ways to pass a config file to a Bootique app, roughly falling in two categories - files contributed via DI and files passed on command line. Let's discuss them one by one: 

* Contributing a config file via DI:

```java
BQCoreModule.extend(binder).addConfig("classpath:com/foo/default.yml");
```

A primary motivation for this style is to provide application default configuration, with YAML files often embedded in the app and read from the classpath (as suggested by the "classpath:.." URL in the example). More then one configuration can be contributed. E.g. individual modules might load their own defaults. Multiple configs are combined in a single config tree by the runtime. The order in which this combination happens is undefined, so make sure there are no conflicts between them. If there are, consider replacing multiple conflicting configs with a single config.

* Conditionally contributing a config file via DI. It is possible to make DI configuration inclusion conditional on the presence of a certain command line option:

```java
OptionMetadata o = OptionMetadata.builder("qa")
      .description("when present, uses QA config")
      .build();

BQCoreModule.extend(binder)
      .addOption(o)
      .addConfigOnOption(o.getName(), "classpath:a/b/qa.yml");
```

* Specifiying a config file on command line. Each Bootique app supports `--config` option that takes a configuration file as its parameter. To specify more than one file, use `--config` option multiple times. Configurations will be loaded and merged together in the order of their appearance on the command line. 

* Specifying a single config value via a custom option:

```java
OptionMetadata o = OptionMetadata.builder("db")
      .description("specifies database URL")
      .configPath("jdbc.mydb.url")
      .defaultValue("jdbc:mysql://127.0.0.1:3306/mydb")
      .build();

BQCoreModule.extend(binder).addOption(o);
```
This adds a new  `--db` option to the app that can be used to set JDBC URL of a datasource called "mydb". If not specified, the default value provided in the code is used.


#### Configuration via Properties

YAML file can be thought of as a set of nested properties. E.g. the following config

```yaml
my:
  prop1: val1
  prop2: val2
```

can be represented as two properties ("my.prop1", "my.prop2") being assigned some values. Bootique takes advantage of this structural equivalence and allows to define configuration via properties as an alternative (or more frequently - an addition) to YAML. If the same "key" is defined in both YAML file and a property, `ConfigurationFactory` would use the value of the property (in other words properties override YAML values).

To turn a given property into a configuration property, you need to prefix it with "`bq.`". This "namespace" makes configuration explicit and helps to avoid random naming conflicts with properties otherwise present in the system.

Properties can be provided to Bootique via BQCoreModule extender:

```java
class MyModule implements Module {
    public void configure(Binder binder) {

        BQCoreModule.extend(binder)
          .setProperty("bq.my.prop1", "valX");

        BQCoreModule.extend(binder)
               .setProperty("bq.my.prop2", "valY");
    }
}
```

Alternatively they can be loaded from system properties. E.g.:

```text
java -Dbq.my.prop1=valX -Dbq.my.prop2=valY -jar myapp.jar
```

Though generally this approach is sneered upon, as the authors of Bootique are striving to make Java apps look minimally "weird" in deployment, and "-D" is one of those unintuitive "Java-only" things. Often a better alternative is to define the bulk of configuration in YAML, and pass values for a few environment-specific properties via shell variables (see the next section) or bind them to CLI flags.

#### Configuration via Environment Variables

Bootique allows to use _environment variables_ to specify/override configuration values. While variables work similar to JVM properties, using them has advantages in certain situations:

* They may be used to configure credentials, as unlike YAML they won't end up in version control, and unlike Java properties, they won't be visible in the process list.
* They provide customized application environment without changing the launch script and are ideal for containerized and other virtual environments.
* They are more user-friendly and appear in the app help.

To declare variables associated with configuration values, use the following API (notice that no "bq." prefix is necessary here to identify the configuration value):

```java
class MyModule implements Module {
    public void configure(Binder binder) {

        BQCoreModule.extend(binder)
               .declareVar("my.prop1", "P1");

        BQCoreModule.extend(binder)
               .declareVar("my.prop2", "P2");
    }
}
```

So now a person running the app may set the above configuration as

```bash
export P1=valX
export P2=valY
```

Moreover, explicitly declared vars will automatically appear in the application help, assisting the admins in configuring your app

_(TODO: document BQConfig and BQConfigProperty config factory annotations required for the help generation to work)_

```bash
$ java -jar myapp-1.0.jar --help
...
ENVIRONMENT
      P1
           Sets value of some property.

      P2
           Sets value of some other property.
```

> Notice that previously used naming conventions to bind variables that start with `BQ_*` to config values are deprecated and support for them will be removed soon. Such approach was causing too much unexpected behavior in non-containerized environments. The alternative is explicitly declared variables described above.

#### Polymorphic Configuration Objects

A powerful feature of Jackson is the ability to dynamically create subclasses of the configuration objects. Bootique takes full advantage of this. E.g. imagine a logging module that needs "appenders" to output its log messages (file appender, console appender, syslog appender, etc.). The framework might not be aware of all possible appenders its users might come up with in the future. Yet it still wants to have the ability to instantiate any of them, based solely on the data coming from YAML. Moreover each appender will have its own set of incompatible configuration properties. In fact this is exactly the situation with `bootique-logback` module.

Here is how you ensure that such a polymorphic configuration is possible. Let's start with a simple class hierarchy and a factory that contains a variable of the supertype that we'd like to init to a concrete subclass in runtime:

```java
public abstract class SuperType {
    // ...
}

public class ConcreteType1 extends SuperType {
    // ...
}

public class ConcreteType2 extends SuperType {
    // ...
}

public class MyFactory {

    // can be a class or an interface
    private SuperType subconfig;
    

    public void setSubconfig(SuperType s) {
        this.subconfig = s;
    }

    // ...
}
```

To make polymorphism work, we need to provide some instructions to Jackson. First we need to annotate the supertype and subtypes:

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, 
     property = "type", 
     defaultImpl = ConcreteType1.class)
public abstract class SuperType {

}

@JsonTypeName("type1")
public class ConcreteType1 extends SuperType {

}

@JsonTypeName("type2")
public class ConcreteType2 extends SuperType {

}
```

After that we need to create a service provider file called `META-INF/service/io.bootique.config.PolymorphicConfiguration` where all the types participating in the hierarchy are listed (including the supertype):

```text
com.foo.SuperType
com.foo.ConcreteType1
com.foo.ConcreteType2
```
This should be enough to work with configuration like this:

```yaml
my:
  subconfig:
    type: type2
    someVar: someVal
```

If another module decides to create yet another subclass of SuperType, it will need to create its own `META-INF/service/io.bootique.config.PolymorphicConfiguration` file that mentions the new subclass.

### Chapter 8. Using Modules

Modules can use other "upstream" modules in a few ways:

* "Import": a downstream module uses another module as a library, ignoring its injectable services.
* "Use" : downstream module's classes inject classes from an upstream module.
* "Contribute": downstream module injects objects to collections and maps defined in upstream modules.

Import case is trivial, so we'll concentrate on the two remaining scenarios. We will use [BQCoreModule](https://github.com/bootique/bootique/blob/master/bootique/src/main/java/io/bootique/BQCoreModule.java) as an example of an upstream module, as it is available in all apps.

#### Injecting Other Module's Services

You can inject any services declared in other modules. E.g. BQCoreModule provides a number of objects and services that can be accessed via injection:

```java
class MyService {

    @Args
    @Inject
    private String[] args;

    public String getArgsString() {
        return Arrays.asList(getArgs()).stream().collect(joining(" "));
    }
}
```

In this example we injected command line arguments that were used to start the app. Note that since there can potentially be more than one `String[]` in a DI container, Bootique `@Args` annotation is used to uniquely identify the array that we want here.

#### Contributing to Other Modules

Guice supports [multibindings](https://github.com/google/guice/wiki/Multibindings), intended to _contribute_ objects defined in a downstream module to collections/maps used by services in upstream modules. Bootique hides Guice API complexities, usually providing "extenders" in each module. E.g. the following code adds `MyCommand` the the app set of commands:

```java
public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
```

Here we obtained an extender instance via a static method on BQCoreModule. Most standard modules define their own extenders accessible via `"extend(Binder)"`. This is a pattern you might want to follow in your own modules.

### Chapter 9. Application Class

A class that contains the `"main()"` method is informally called "application". Bootique does not impose any additional requirements on this class. You decide what to put in it. It can be limited to just `"main()"`, or turned into a REST API resource, etc.

#### Application as a Module

Most often then not it makes sense to turn the application class into a Module though. After all a Bootique app is just a collection of Modules, and this way the application class would represent that one final Module to rule them all:

```java
public class Application implements Module {

   public static void main(String[] args) {
      Bootique.app(args).module(Application.class).autoLoadModules().exec().exit();
   }

   public void configure(Binder binder) {
      // load app-specific services; redefine standard ones
   }
}
```

You may also implement a separate BQModuleProvider for the Application module. Then `autoLoadModules()` will discover it just like any other Module, and there won't be a need to add Application module explicitly.

#### Common Main Class

If all your code is packaged in auto-loadable modules (which is always a good idea), you may not even need a custom main class. `io.bootique.Bootique` class itself declares a `main()` method and can be used as an app launcher. This creates some interesting possibilities. E.g. you can create Java projects that have no code of their own and are simply collections of modules declared as compile dependencies. More details on packaging are given in the "Runnable Jar" chapter.

### Chapter 10. Commands

Bootique runtime contains a set of commands coming from Bootique core and from all the modules currently in effect in the app. On startup Bootique attempts to map command-line arguments to a single command type. If no match is found, a _default_ command is executed (which is normally a "help" command). To list all available commands, the app can be run with `--help` option (in most cases running without any options will have the same effect). E.g.:

```bash
$ java -jar myapp-1.0.jar --help

NAME
      com.foo.MyApp

OPTIONS
      -c yaml_location, --config=yaml_location
           Specifies YAML config location, which can be a file path or a URL.

      -h, --help
           Prints this message.

      -H, --help-config
           Prints information about application modules and their configuration
           options.

      -s, --server
           Starts Jetty server.

```

#### Writing Commands

Most common commands are already available in various standard modules, still often you'd need to write your own. To do that, first create a command class. It should implement `io.bootique.command.Command` interface, though usually it more practical to extend `io.bootique.command.CommandWithMetadata` and provide some metadata used in help and elsewhere:

```java
public class MyCommand extends CommandWithMetadata {

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(MyCommand.class)
                .description("My command does something important.")
                .build();
    }

    public MyCommand() {
        super(createMetadata());
    }

    @Override
    public CommandOutcome run(Cli cli) {

        // ... run the command here....

        return CommandOutcome.succeeded();
    }
}
```

The command initializes metadata in constructor and implements the "run" method to run its code. The return CommandOutcome object instructs Bootique what to do when the command finishes. The object contains desired system exit code, and exceptions that occurred during execution. To make the new command available to Bootique, add it to `BQCoreModule`'s extender, as was already shown above:

```java
public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
```

To implement a "daemon" command running forever until it receives an OS signal (e.g. a web server waiting for user requests) , do something like this:

```java
@Override
public CommandOutcome run(Cli cli) {

    // ... start some process in a different thread ....

    // now wait till the app is stopped from another thread 
    // or the JVM is terminated
    try {
        Thread.currentThread().join();
    } catch (InterruptedException e) {
        // ignore exception or log if needed
    }

    return CommandOutcome.succeeded();
}
```

#### Injection in Commands

Commands can inject services, just like most other classes in Bootique. There are some specifics though. Since commands are sometimes instantiated, but not executed (e.g. when `--help` is run that lists all commands), it is often desirable to avoid immediate instantiation of all dependencies of a given command. So a common pattern with commands is to inject Guice `Provider` instead of direct dependency:

```java
@Inject
private Provider<SomeService> provider;

@Override
public CommandOutcome run(Cli cli) {
    provider.get().someMethod();
}
```

#### Decorating Commands

Each command typically does a single well-defined thing, such as starting a web server, executing a job, etc. But very often in addition to that main thing you need to do other things. E.g. when a web server is started, you might also want to run a few more commands:

* Before starting the server, run a health check to verify that any external services the app might depend upon are alive.
* Start a job scheduler in the background.
* Start a monitoring "heartbeat" thread.

To run all these "secondary" commands when the main command is invoked, Bootique provides command decorator API. First you create a decorator policy object that specifies one or more secondary commands and their invocation strategy (either *before* the main command, or *in parallel* with it). Second you "decorate" the main command with that policy:

```java
CommandDecorator extraCommands = CommandDecorator
  .beforeRun(CustomHealthcheckCommand.class)
  .alsoRun(ScheduleCommand.class)
  .alsoRun(HeartbeatCommand.class);

BQCoreModule.extend(binder).decorateCommand(ServerCommand.class, extraCommands);
```
Based on the specified policy Bootique figures out the sequence of execution and runs the main and the secondary commands.

### Chapter 11. Options

#### Simple Options

In addition to commands, the app can define "options". Options are not associated with any runnable java code, and simply pass command-line values to commands and services. E.g. the standard "`--config`" option is used by `CliConfigurationSource` service to locate configuration file. Unrecognized options cause application startup errors. To be recognized, options need to be "contributed" to Bootique similar to commands:

```java
CliOption option = CliOption
    .builder("email", "An admin email address")
    .valueRequired("email_address").build();

BQCoreModule.extend(binder).addOption(option);
```

To read a value of the option, a service should inject `io.bootique.cli.Cli` object (commands also get this object as a parameter to "run") :

```java
@Inject
private Cli cli;

public void doSomething() {
    Collection<String> emails = cli.optionStrings("email");
    // do something with option values....
}
```

#### Configuration Options

While you can process your own options as described above, options often are just aliases to enable certain pieces of configuration. Bootique supports three flavors of associating options with configuration. Let's demonstrate them here.

1. Option value sets a config property:

    ```java
    // Starting the app with "--my-opt=x" will set "jobs.myjob.param" value to "x"
    BQCoreModule.extend(binder).addOption("jobs.myjob.param", "my-opt");
    ```
2. Option presence sets a property to a predefined value:

    ```java
    // Starting the app with "--my-opt" will set "jobs.myjob.param" value to "y"
    BQCoreModule.extend(binder).addOption("jobs.myjob.param", "y", "my-opt");
    ```
3. Option presence loads a config resource, such as a YAML file:

    ```java
    // Starting the app with "--my-opt" is equivalent to starting with "--config=classpath:xyz.yml"
    BQCoreModule.extend(binder).addConfigFileOption("classpath:xyz.yml", "my-opt");
    ```

The order of config-bound options on the command line is significant, just as the order of "`--config`" parameters. Bootique merges configuration associated with options from left to right, overriding any preceding configuration if there is an overlap.

### Chapter 12. Logging

#### Loggers in the Code

tandard Bootique modules use [SLF4J](http://www.slf4j.org/) internally, as it is the most convenient least common denominator framework, and can be easily bridged to other logging implementations. Your apps or modules are not required to use SLF4J, though if they do, it will likely reduce the amount of bridging needed to route all logs to a single destination.

#### Configurable Logging with Logback

For better control over logging a standard module called `bootique-logback` is available, that integrates [Logback framework](http://logback.qos.ch/) in the app. It seamlessly bridges SLF4J (so you keep using SLF4J in the code), and allows to configure logging via YAML config file, including appenders (file, console, etc.) and per class/package log levels. Just like any other module, `bootique-logback` can be enabled by simply adding it to the pom.xml dependencies, assuming `autoLoadModules()` is in effect:

```xml
<dependency>
    <groupId>io.bootique.logback</groupId>
    <artifactId>bootique-logback</artifactId>
</dependency>
```

See `bootique-logback` module [documentation](http://bootique.io/docs/0/bootique-logback-docs/) for further details.

#### BootLogger

To perform logging during startup, before DI environment is available and YAML configuration is processed, Bootique uses a special service called `BootLogger`, that is not dependent on SLF4J and is not automatically bridged to Logback. It provides an abstraction for writing to stdout / stderr, as well as conditional "trace" logs sent to stderr. To enable Bootique trace logs, start the app with `-Dbq.trace` as described in the deployment section.

BootLogger is injectable, in case your own code needs to use it. If the default BootLogger behavior is not satisfactory, it can be overridden right in the `main(..)` method, as unlike other services, you may need to change it before DI is available:

```java
public class Application {
  public static void main(String[] args) {
     Bootique.app(args).bootLogger(new MyBootLogger()).run();
  }
}
```

## Part III. Testing

### Chapter 13. Bootique and Testing

Bootique is uniquely suitable to be used as a test framework. Within a single test it allows you to start and stop multiple embedded stacks with distinct set of modules and distinct YAML configurations, making it a powerful tool for _integration testing._ Bootique core module and some other modules provide companion test extensions that contain reusable test stacks.

### Chapter 14. Creating Test Stacks

To use basic Bootique test framework, import the following module in the "test" scope:

```xml
<dependency>
    <groupId>io.bootique</groupId>
    <artifactId>bootique-test</artifactId>
    <scope>test</scope>
</dependency>
```

For module-specific "companion" test frameworks (e.g. `bootique-jetty-test`), check documentation of those modules or GitHub.

While there are a number of built-in and custom stacks that you can create, they usually fall into two broad categories - "foreground" - those that are running in the main test thread, and "background" - those that are running in an isolated thread pool (usually network services like Jetty). To create a foreground stack, use `BQTestFactory`, annotated with `@Rule` or `@ClassRule`:

```java
public class ForegroundTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testXyz() {
        BQRuntime runtime = testFactory.app("--help").createRuntime();
        // ...
    }
}
```

As you see, the test class declares a factory, and test methods can create `BQRuntime` instances with different command-line arguments, including commands (`--help` in this example), configuration file (`"--config=some.yml"`), etc. So your test runtime will behave just like a real Java app and will allow to verify various scenarios.

If we need to start the app on background (e.g. we are starting a webserver), it needs to be started with `BQDaemonTestFactory` instead of `BQTestFactory`:

```java
public class BackgroundTest {

    @Rule
    public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();
    
    @Test
    public void testBackground() {
        BQRuntime runtime = testFactory.app("--server").start();
        // ... 
    }
}
```

You don't need to stop it explicitly. `BQDaemonTestFactory` will take care of it via JUnit lifecycle.

The next thing you may want to do is to add various modules to the basic stack (either foreground or background). `testFactory.app()` returns a builder object that allows loading extra modules to add or override runtime services. This API is designed to mimic `Bootique` class, so that your tests look similar to actual applications:

```java
@Test
public void testAbc() {

    testFactory.app("--help")
        // ensure all classpath modules are included
        .autoLoadModules()
        // add an adhoc module specific to the test
        .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
        .createRuntime();
    // ... 
}
```

### Chapter 15. Common Test Scenarios

Now that we can start stacks on foreground or background, we can finally write some tests. Some things that can be tested include runtime services with real dependencies, standard output of full Bootique applications (i.e. the stuff that would be printed to console if this were a real app), network services using real network connections (e.g. your REST API's), and so on. Some examples are given below, outlining common techniques.

#### Testing Services that are Part of Bootique Runtime

Services can be obtained from test runtime, their methods called, and assertions made about the results of the call:

```java
@Test
public void testService() {

    BQRuntime runtime = testFactory.app("--config=src/test/resources/my.yml").createRuntime();

    MyService service = runtime.getInstance(MyService.class);
    assertEquals("xyz", service.someMethod());
}
```

#### Testing Network Services

If a test stack is started on the background, and if runs a web server (like `bootique-jetty-test`) or some other network service, it can be accessed via a URL. E.g.:

```java
@Test
public void testServer() {

    BQRuntime runtime = testFactory.app("--server").start();

    // using JAX-RS client API
    WebTarget base = ClientBuilder.newClient().target("http://localhost:8080/");
    Response r1 = base.path("/somepath").request().get();
    assertEquals(Status.OK.getStatusCode(), r1.getStatus());
    assertEquals("{}", r1.readEntity(String.class));
}
```

#### Testing Commands

You can emulate a real app execution in a unit test, by running a command and then checking the values of the exist code and `stdin` and `stderr` contents:

```java
@Test
public void testCommand() {

    TestIO io = TestIO.noTrace();
    CommandOutcome outcome = testFactory
        .app("--help")
        .bootLogger(io.getBootLogger())
        .createRuntime()
        .run();

    assertEquals(0, outcome.getExitCode());
    assertTrue(io.getStdout().contains("--help"));
    assertTrue(io.getStdout().contains("--config"));
}
```

#### Testing Module Validity

When you are writing your own modules, you may want to check that they are configured properly for autoloading (i.e. `META-INF/services/io.bootique.BQModuleProvider` is present in the expected place and contains the right provider. There's a helper class to check for it:

```java
@Test
public void testPresentInJar() {
    BQModuleProviderChecker.testPresentInJar(MyModuleProvider.class);
}
```

## Part IV. Assembly and Deployment

### Chapter 16. Runnable Jar

To build a runnable jar, Bootique relies on `maven-shade-plugin`. To simplify its configuration, your app `pom.xml` may inherit from `bootique-parent` pom. In this case configuration would look like this:

```xml
<parent>
    <groupId>io.bootique.parent</groupId>
    <artifactId>bootique-parent</artifactId>
    <version>0.12</version>
</parent>

...
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

This configuration will build an app with the framework-provided main class, namely `io.bootique.Bootique`. If you want to use a custom main class (and in most cases you do), you will need to redefine Maven `main.class` property:

```xml
<properties>
    <main.class>com.foo.Application</main.class>
</properties>
```

If you want to avoid inheriting from the framework parent pom, you will need to explicitly provide the following unwieldy configuration similar to the one found in [`bootique-parent`](https://repo1.maven.org/maven2/io/bootique/parent/bootique-parent/0.12/bootique-parent-0.12.pom):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>2.4.2</version>

    <configuration>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <filters>
            <filter>
                <artifact>*:*</artifact>
                <excludes>
                    <exclude>META-INF/*.SF</exclude>
                    <exclude>META-INF/*.DSA</exclude>
                    <exclude>META-INF/*.RSA</exclude>
                </excludes>
            </filter>
        </filters>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>${main.class}</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Either way, once your pom is configured, you can assemble and run your jar. E.g.:

```bash
mvn clean package
java -jar target/myapp-1.0.jar
```

### Chapter 17. Tracing Bootique Startup

To see what modules are loaded and to trace other events that happen on startup, run your jar with `-Dbq.trace` option. E.g.:

```bash
java -Dbq.trace -jar target/myapp-1.0.jar --server
```

You may see an output like this:

```text
Skipping module 'JerseyModule' provided by 'JerseyModuleProvider' (already provided by 'Bootique')...
Adding module 'BQCoreModule' provided by 'Bootique'...
Adding module 'JerseyModule' provided by 'Bootique'...
Adding module 'JettyModule' provided by 'JettyModuleProvider'...
Adding module 'LogbackModule' provided by 'LogbackModuleProvider'...
```

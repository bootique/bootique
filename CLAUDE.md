# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build all modules
mvn clean verify

# Build a single module (e.g. core)
mvn -am -pl bootique clean verify

# Run all tests in a module
mvn -am -pl bootique test

# Run a single test class
mvn -am -pl bootique test -Dtest=BootiqueIT

# Run a single test method
mvn -am -pl bootique test -Dtest=BootiqueIT#exec

# Skip tests (fast build)
mvn -am -pl bootique clean package -DskipTests
```

Tests are split by convention: `*Test.java` for unit tests, `*IT.java` for integration tests.

## Architecture

Bootique is a Java application framework built around two core concepts: a custom DI container and a module system.

### DI Container (`io.bootique.di`)

Bootique ships its **own DI container** — it does not use Spring or Guice. The entry point is `DIBootstrap`. The `Injector` interface is the runtime container; `Binder` is the configuration-time API passed to modules.

Key DI features:
- `bind(Type.class).to(Impl.class)` — standard binding
- `bindSet(Type.class)` / `bindMap(K.class, V.class)` — injectable collections (contributors add to these from different modules)
- `bindOptional(Type.class)` — binding that may not exist
- `decorate(Type.class)` — wraps an existing binding (decorator pattern)
- `override(Type.class)` — replaces an existing binding
- `@Provides` methods in modules as an alternative to `bind()` calls
- Default scope is singleton; use `.in(scope)` to change

Implementation lives in `io.bootique.di.spi` and should not be touched directly.

### Module System (`io.bootique.BQModule`)

Every feature is packaged as a `BQModule` — a `@FunctionalInterface` with a single `configure(Binder)` method. Modules are discovered at runtime via Java SPI (`META-INF/services/io.bootique.BQModule`) when `autoLoadModules()` is called.

`ModuleCrate` wraps a module with metadata (name, description, which bindings it overrides, which config types it contributes). Modules that want to expose metadata override `BQModule.crate()` using the `ModuleCrate.of(this)` builder.

`ModuleExtender<T>` is the base class for the "extender" pattern: each module exposes a static factory method (e.g., `BQCoreModule.extend(binder)`) that returns an extender, letting other modules add to its collections without knowing the binding keys.

### App Lifecycle

```java
Bootique.app(args)
    .autoLoadModules()   // SPI-discover BQModules
    .module(myModule)    // add programmatic modules
    .exec()              // select and run a Command
    .exit();             // call System.exit with the outcome code
```

`BQRuntime` is the assembled application container. `Bootique` is the builder. After `exec()`, a `CommandOutcome` carries success/failure and the exit code.

### Commands (`io.bootique.command`)

Commands are DI-managed objects implementing `Command`. `BQCoreModule` binds the standard commands (`HelpCommand`, `HelpConfigCommand`). User modules contribute commands via `BQCoreModule.extend(binder).addCommand(MyCommand.class)`. `CommandManager` resolves which command to run from CLI args.

### Configuration (`io.bootique.config`)

Configuration is YAML/JSON loaded via Jackson. `ConfigurationFactory` is the injected factory; call `factory.config(MyConfig.class, "prefix")` to bind a YAML subtree to a POJO. `@BQConfig` and `@BQConfigProperty` annotations on config POJOs enable help generation and validation.

`PolymorphicConfiguration` is the base for config types that need a `type` discriminator (polymorphic deserialization). Config sources are stacked: files → CLI `--config` option → environment variables (mapped via `DeclaredVariable`).

### Testing (`bootique-junit`)

Use `@BQTest` on the test class. Inject a `BQTestFactory` with `@RegisterExtension` (JUnit 6 pattern). Call `testFactory.app("--config=path.yml").modules(...).createRuntime()` to build an isolated runtime per test. `PolymorphicConfigurationChecker` and `BQModuleTester` are utilities for testing module metadata.

## Key Naming Conventions

| Pattern | Meaning |
|---|---|
| `BQ*` | Core framework types (`BQModule`, `BQRuntime`, `BQCoreModule`) |
| `Default*` | Concrete implementations of framework interfaces |
| `*Extender` | Module extension builder (returned from `SomeModule.extend(binder)`) |
| `*Metadata` | Compile-time metadata objects for configs, options, modules |
| `*IT.java` | Integration test (uses a full Bootique runtime) |
| `*Test.java` | Unit test |

## Module Layout

- `bootique/` — core framework: DI container, module system, CLI, config loading, commands
- `bootique-junit/` — JUnit 6 test utilities (`@BQTest`, `BQTestFactory`)
- `bootique-jsr330-tck/` — JSR-330 compliance tests run against the custom DI container
- `bootique-testcontainers-internal/` — shared Testcontainers utilities used by other test modules
- `bootique-junit-badspi-it/` — integration tests for malformed SPI configurations
- `bootique-docs/` — documentation build (AsciiDoc → HTML)

## License

Apache License 2.0. All `.java` files require the standard Apache license header. 

## Anti-patterns

* Jakarta EE APIs are used (`jakarta.inject.*`), not `javax.*`.
* Old Bootique used Guice for injection. Not anymore. There is no Guice anywhere for a long time

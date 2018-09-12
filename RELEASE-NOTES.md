## 1.0.RC1

* #188 Update Guice to 4.2.0
* #189 Update Jackson to 2.9.5
* #190 StackOverflowError on -H and recursive factories
* #213 Excluded Guava subdependencies
* #214 Cleaning up APIs deprecated since <= 0.25
* #215 Remove support for date/time values as arrays of components
* #216 Config: addressing array elements via property paths
* #219 Value object: Duration
* #218 Value object: Percent
* #224 Value object: Bytes
* #226 Add copyright to all sources
* #228 Integrate Apache Rat in modules for license checking
* #232 Upgrade bootique and core modules to bootique-modules-parent:0.11
* #235 Upgrade bootique and core modules to bootique-modules-parent:0.12

## 0.25 

* #131 Complex command invocation structures
* #178 Config CLI options order
* #193 JsonNodeConfigurationFactory - proper parser initialization
* #194 Refactor JoptCliProvider to CliFactory and generic Cli provider
* #195 CommandManager: allow lookup of commands by name and by type
* #196 CommandManager: track command attributes and private commands
* #197 Commands starting background processes should unblock invoker threads
* #198 DI config binding API to facilitate configuration reuse
* #199 Tests: swallowed BQDaemonTestFactory startup errors
* #201 Speed up BQDaemonTestFactory
* #211 BQRuntimeChecker - static helper for runtime assertions
* #212 Rename "BQModuleProviderChecker.testPresentInJar" to "testAutoLoadable" 

## 0.24

* #111 Config CLI options
* #155 Unit tests inherit shell env vars, ignoring test configuration 
* #173 CLI Options duplicate names check
* #175 Deprecating BQ_* vars
* #180 Case-sensitive property resolution for declared vars
* #183 Module aggregation / define module dependencies
* #185 Show full dependency override graph

## 0.23

* #25  Map known exceptions to CommandOutcomes
* #127 Allow @BQConfigProperty to be used on constructors 
* #140 Catch Guice CreationException
* #141 Refactor startup sequence
* #142 Streamline Test API - hide BQTestRuntime wrapper
* #148 Exceptions inside CommandOutcomes are not reported by Bootique
* #157 Can't access grandparent service in a two-level override
* #158 Allow access to DI contents by Guice Key

## 0.22

* #90  Module "extend" API - a better version of `contribute*`
* #119 Relax @BQConfigProperty restrictions.
* #123 ENVIRONMENT section in help
* #124 Exposing and aliasing app config environment variables 
* #133 Environment variables not merged into configuration

## 0.21

* #27 ConfigHelpCommand: Help for configs
* #99 Empty YAML troubles
* #100 Default command options are visible in help, but can't be used
* #102 Metadata model for modules and their configs
* #105 Default command name generation should convert camel-case to dashes 
* #112 Explicit short option names 
* #113 Move io.bootique.application to io.bootique.meta.application
* #114 Config metadata: Support for polymorphic types
* #115 Config metadata: Fold long descriptions

## 0.20

* #89 Don't print debug info to stdout
* #91 bootique-test: test helper for PolymorphicConfiguration
* #92 Standalone service for printing help
* #93 A concept of "app name" for help purposes
* #95 Support for terminal width detection
* #96 Separate default and help commands
* #97 Bootique-like builder API for unit tests
* #98 Support setting app text description to use in help

## 0.19

* #84 Cache SubtypeResolver in DefaultJacksonService
* #85 API to override log levels
* #87 Move to io.bootique namespace

## 0.18

* #73 Test ResourceFactory and FolderResourceFactory on Windows
* #76 BQDaemonTestRuntime fails quietly.

## 0.17

* #62 Bootique.main() - a generic main method
* #63 Must canonicalize file:/ URLs.
* #65 Allow property override from environment variables
* #66 Support for multiple ordered configs
* #67 Expand Bootique.app(..) method
* #69 Appendable args list
* #71 Support for alternative formats of configs
* #72 FolderResourceFactory classpath: URL issues 
* #74 Remove API deprecated since 0.12

## 0.16:

* #57 BQDaemonTestFactory - return BQDaemonTestRuntime from Builder.start
* #58 BQDaemonTestRuntime to track outcome
* #59 BQTestRuntime.run(..) method

## 0.15:

* #38 BQDaemonTestRuntime NPE in stop if there was an error in start
* #39 A wrapper for BQDaemonTestRuntime injectable to JUnit
* #41 A wrapper for BQTestRuntime injectable to JUnit
* #44 BQModuleProviderChecker: a test helper that ensures module provider 
      is available for auto-loading
* #46 Support classpath: URLs in configuration loader.
* #47 ResourceFactory: Configuration type for abstract resource
* #54 @BindingAnnotation's in Bootique should be allowed on methods

## 0.14:

* #32 A test module to write Bootique apps for unit tests
* #34 BQRuntime.getInstance(..) must throw on missing bindings

## 0.13:

* #14 Support for configs polymorphism
* #31 Rename Bootique.runtime() to Bootique.createRuntime()

## 0.12:

* #17 Wrap CLI Options API to avoid direct dependency on JOpt lib in commands
* #18 Separate a concept of DI-bound option from Command
* #19 Command dispatch mechanism based on Cli state
* #20 Allow configuration of available commands in main()
* #21 Move contribution API into static methods on BQCoreModule
* #22 Better Module override API
* #24 Allow creation of DefaultBootLogger with non-System stdin/out
* #28 --config option should support both files and URLs

## 0.11:

* #8 Remove API's deprecated since 0.10
* #10 Service shutdown functionality

## 0.10:

* #5 FactoryConfigurationService refactoring
* #6 Service/Module override support
* #7 Start publishing Bootique to Maven central repo

## 0.9:

* #4 FactoryConfigurationService - support for loading parameterized types

## 0.8:

* #1 Merge bundles and modules into a single concept
* #2 Auto-load extensions
* #3 Create BootLogger outside DI, add "trace" logging
